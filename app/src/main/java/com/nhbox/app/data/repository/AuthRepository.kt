package com.nhbox.app.data.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ServerValue
import com.nhbox.app.data.model.UserPreferences
import com.nhbox.app.data.model.UserProfile
import com.nhbox.app.data.remote.FirebaseRtdbService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val rtdb: FirebaseRtdbService
) {
    val currentUser: FirebaseUser? get() = auth.currentUser

    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun observeCurrentUserProfile(): Flow<UserProfile?> {
        return authStateFlow.flatMapLatest { user ->
            if (user == null) {
                flowOf(null)
            } else {
                rtdb.observeValue("users/${user.uid}", UserProfile::class.java)
                    .map { it.getOrNull() }
            }
        }
    }

    suspend fun signIn(email: String, password: String): Result<UserProfile> = runCatching {
        val authResult = auth.signInWithEmailAndPassword(email.trim(), password).await()
        val uid = authResult.user?.uid ?: error("Authentication failed")
        val profile = rtdb.getValueOnce("users/$uid", UserProfile::class.java).getOrNull()
            ?: UserProfile(
                uid = uid,
                username = email.substringBefore("@"),
                email = email.trim(),
                role = "user",
                accountStatus = "active",
                isOnline = true,
                createdAt = System.currentTimeMillis(),
                lastActive = System.currentTimeMillis()
            ).also {
                rtdb.setValue("users/$uid", it).getOrThrow()
            }

        if (profile.accountStatus == "banned" || profile.accountStatus == "suspended") {
            auth.signOut()
            error("Your account is ${profile.accountStatus}. Contact support.")
        }

        setupPresence(uid)
        profile
    }

    suspend fun register(username: String, email: String, password: String): Result<UserProfile> =
        runCatching {
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val uid = authResult.user?.uid ?: error("Registration failed")
            val now = System.currentTimeMillis()
            val profile = UserProfile(
                uid = uid,
                username = username.trim(),
                email = email.trim(),
                role = "user",
                accountStatus = "active",
                isOnline = true,
                createdAt = now,
                lastActive = now,
                preferences = UserPreferences()
            )
            rtdb.setValue("users/$uid", profile).getOrThrow()
            setupPresence(uid)
            profile
        }

    suspend fun setupPresence(uid: String) {
        runCatching {
            val userRef = rtdb.ref("users/$uid")
            userRef.child("online").onDisconnect().setValue(false)
            userRef.child("lastActive").onDisconnect().setValue(ServerValue.TIMESTAMP)
            userRef.updateChildren(
                mapOf(
                    "online" to true,
                    "lastActive" to System.currentTimeMillis()
                )
            ).await()
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> =
        runCatching {
            val user = auth.currentUser ?: error("Not signed in")
            val email = user.email ?: error("Email not found")
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential).await()
            user.updatePassword(newPassword).await()
            Unit
        }

    suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email.trim()).await()
        Unit
    }

    suspend fun updateProfile(username: String, profileImageId: String? = null): Result<Unit> =
        runCatching {
            val uid = auth.currentUser?.uid ?: error("Not signed in")
            val updates = mutableMapOf<String, Any?>(
                "username" to username.trim(),
                "lastActive" to System.currentTimeMillis()
            )
            if (profileImageId != null) {
                updates["profileImageId"] = profileImageId
            }
            rtdb.updateChildren("users/$uid", updates).getOrThrow()
        }

    suspend fun updatePreferences(preferences: UserPreferences): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: error("Not signed in")
        rtdb.setValue("users/$uid/preferences", preferences).getOrThrow()
    }

    suspend fun signOut() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            runCatching {
                rtdb.updateChildren(
                    "users/$uid",
                    mapOf("online" to false, "lastActive" to System.currentTimeMillis())
                )
            }
        }
        auth.signOut()
    }
}
