package com.nhbox.app.data.remote

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRtdbService @Inject constructor(
    val database: FirebaseDatabase
) {
    fun ref(path: String): DatabaseReference = database.getReference(path)

    fun <T> observeValue(path: String, clazz: Class<T>): Flow<Result<T?>> = callbackFlow {
        val reference = ref(path)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    trySend(Result.success(snapshot.getValue(clazz)))
                } catch (e: Exception) {
                    trySend(Result.failure(e))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(error.toException()))
            }
        }
        reference.addValueEventListener(listener)
        awaitClose { reference.removeEventListener(listener) }
    }

    fun <T> observeList(
        query: Query,
        clazz: Class<T>
    ): Flow<Result<List<T>>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val items = snapshot.children.mapNotNull { it.getValue(clazz) }
                    trySend(Result.success(items))
                } catch (e: Exception) {
                    trySend(Result.failure(e))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(error.toException()))
            }
        }
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    suspend fun <T> getValueOnce(path: String, clazz: Class<T>): Result<T?> = runCatching {
        val snapshot = ref(path).get().await()
        snapshot.getValue(clazz)
    }

    suspend fun <T> getListOnce(query: Query, clazz: Class<T>): Result<List<T>> = runCatching {
        val snapshot = query.get().await()
        snapshot.children.mapNotNull { it.getValue(clazz) }
    }

    suspend fun setValue(path: String, value: Any?): Result<Unit> = runCatching {
        ref(path).setValue(value).await()
        Unit
    }

    suspend fun updateChildren(path: String, updates: Map<String, Any?>): Result<Unit> = runCatching {
        ref(path).updateChildren(updates).await()
        Unit
    }

    suspend fun removeValue(path: String): Result<Unit> = runCatching {
        ref(path).removeValue().await()
        Unit
    }

    fun generateKey(path: String): String = ref(path).push().key ?: System.currentTimeMillis().toString()
}
