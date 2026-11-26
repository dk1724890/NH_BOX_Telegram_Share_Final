package com.nhbox.app.data.repository

import android.graphics.Bitmap
import android.net.Uri
import android.util.LruCache
import com.google.firebase.auth.FirebaseAuth
import android.content.Context
import com.nhbox.app.data.model.RtdbImage
import com.nhbox.app.security.NhCreditVault
import dagger.hilt.android.qualifiers.ApplicationContext
import com.nhbox.app.data.remote.FirebaseRtdbService
import com.nhbox.app.utils.image.compression.ImageCompressionEngine
import com.nhbox.app.utils.image.compression.ImageProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class ImageUploadProgress(
    val compressionProgress: Float = 0f,
    val uploadProgress: Float = 0f,
    val statusText: String = ""
)

@Singleton
class ImageRtdbRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val rtdb: FirebaseRtdbService,
    private val auth: FirebaseAuth,
    private val compressionEngine: ImageCompressionEngine
) {
    private val memoryCache = object : LruCache<String, Bitmap>(48) {}

    suspend fun uploadCompressedImage(
        uri: Uri,
        profile: ImageProfile,
        maxSizeKb: Int = 180,
        onProgress: (ImageUploadProgress) -> Unit = {}
    ): Result<String> = runCatching {
        val ownerId = auth.currentUser?.uid ?: error("Please sign in first")
        onProgress(ImageUploadProgress(0.05f, 0f, "Compressing image..."))

        val compressed = compressionEngine.compressFromUri(
            uri = uri,
            profile = profile,
            maxSizeKb = maxSizeKb
        ) { p ->
            onProgress(ImageUploadProgress(p, 0f, "Compressing (${(p * 100).toInt()}%)..."))
        }.getOrThrow()

        val imageId = rtdb.generateKey(NhCreditVault.node(context, "images"))
        val entity = RtdbImage(
            id = imageId,
            category = profile.categoryName,
            base64Data = compressed.base64Data,
            mimeType = compressed.mimeType,
            width = compressed.width,
            height = compressed.height,
            sizeBytes = compressed.sizeBytes,
            sha256Hash = compressed.sha256Hash,
            ownerId = ownerId,
            createdAt = System.currentTimeMillis()
        )

        var lastError: Throwable? = null
        val maxAttempts = 3
        for (attempt in 1..maxAttempts) {
            onProgress(ImageUploadProgress(1f, 0.4f + (attempt * 0.15f), "Uploading (${compressed.sizeBytes / 1024} KB)..."))
            val result = rtdb.setValue("${NhCreditVault.node(context, "images")}/$imageId", entity)
            if (result.isSuccess) {
                withContext(Dispatchers.IO) {
                    compressionEngine.decodeBase64ToBitmap(compressed.base64Data)
                }?.let { bmp ->
                    memoryCache.put(imageId, bmp)
                }
                onProgress(ImageUploadProgress(1f, 1f, "Uploaded (${compressed.sizeBytes / 1024} KB)"))
                return@runCatching imageId
            } else {
                lastError = result.exceptionOrNull()
                if (attempt < maxAttempts) delay(500L * attempt)
            }
        }
        throw lastError ?: IllegalStateException("Image upload failed after $maxAttempts attempts")
    }

    suspend fun getBitmap(imageId: String): Bitmap? {
        if (imageId.isBlank()) return null
        val cleanId = imageId.removePrefix("rtdb://")
        memoryCache.get(cleanId)?.let { return it }
        val rtdbImage = rtdb.getValueOnce("${NhCreditVault.node(context, "images")}/$cleanId", RtdbImage::class.java).getOrNull()
            ?: return null
        val bitmap = withContext(Dispatchers.IO) {
            compressionEngine.decodeBase64ToBitmap(rtdbImage.base64Data)
        }
        if (bitmap != null) {
            memoryCache.put(cleanId, bitmap)
        }
        return bitmap
    }

    suspend fun deleteImageIfExists(imageId: String) {
        if (imageId.isBlank()) return
        val cleanId = imageId.removePrefix("rtdb://")
        memoryCache.remove(cleanId)
        rtdb.removeValue("${NhCreditVault.node(context, "images")}/$cleanId")
    }
}
