package com.nhbox.app.utils.image.compression

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.util.Base64
import androidx.exifinterface.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

enum class ImageProfile(
    val maxWidth: Int,
    val maxHeight: Int,
    val preserveAlpha: Boolean,
    val categoryName: String
) {
    POSTER(600, 900, false, "POSTER"),
    BACKDROP(1280, 720, false, "BACKDROP"),
    THUMBNAIL(300, 450, false, "THUMBNAIL"),
    LOGO(600, 300, true, "LOGO"),
    BANNER(1280, 720, false, "BANNER"),
    PROFILE(400, 400, false, "PROFILE"),
    CATEGORY(600, 400, false, "CATEGORY")
}

data class CompressedImageResult(
    val base64Data: String,
    val mimeType: String,
    val width: Int,
    val height: Int,
    val sizeBytes: Int,
    val sha256Hash: String
)

@Singleton
class ImageCompressionEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun compressFromUri(
        uri: Uri,
        profile: ImageProfile,
        maxSizeKb: Int = 180,
        onProgress: (Float) -> Unit = {}
    ): Result<CompressedImageResult> = withContext(Dispatchers.IO) {
        runCatching {
            onProgress(0.1f)
            val decodedBitmap = decodeUriSafely(uri, profile.maxWidth, profile.maxHeight)
            onProgress(0.45f)

            val scaledBitmap = scaleToFit(decodedBitmap, profile.maxWidth, profile.maxHeight)
            onProgress(0.65f)

            val maxBytes = maxSizeKb.coerceIn(60, 300) * 1024
            val format = getCompressFormat()
            val mimeType = "image/webp"

            var quality = 85
            var compressedBytes: ByteArray
            var currentBitmap = scaledBitmap
            var iterations = 0

            while (true) {
                iterations++
                val baos = ByteArrayOutputStream()
                currentBitmap.compress(format, quality, baos)
                compressedBytes = baos.toByteArray()

                if (compressedBytes.size <= maxBytes || iterations >= 12 || (quality <= 25 && currentBitmap.width <= 200)) {
                    break
                }

                if (quality > 30) {
                    quality -= 12
                } else {
                    val nextW = max(160, (currentBitmap.width * 0.75f).toInt())
                    val nextH = max(160, (currentBitmap.height * 0.75f).toInt())
                    currentBitmap = Bitmap.createScaledBitmap(currentBitmap, nextW, nextH, true)
                    quality = 70
                }
            }

            onProgress(0.9f)
            val finalWidth = currentBitmap.width
            val finalHeight = currentBitmap.height
            val base64 = Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
            val sha256 = sha256Hex(compressedBytes)
            onProgress(1.0f)

            CompressedImageResult(
                base64Data = base64,
                mimeType = mimeType,
                width = finalWidth,
                height = finalHeight,
                sizeBytes = compressedBytes.size,
                sha256Hash = sha256
            )
        }
    }

    private fun decodeUriSafely(uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            runCatching {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    val sample = calculateInSampleSize(
                        info.size.width,
                        info.size.height,
                        reqWidth,
                        reqHeight
                    )
                    if (sample > 1) decoder.setTargetSampleSize(sample)
                }
            }
        }

        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, boundsOptions)
        } ?: error("Unable to open selected image")

        if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
            error("Invalid image dimensions")
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(
                boundsOptions.outWidth,
                boundsOptions.outHeight,
                reqWidth,
                reqHeight
            )
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        val rawBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        } ?: error("Failed to decode image")

        return applyExifOrientation(uri, rawBitmap)
    }

    fun decodeBase64ToBitmap(base64Data: String): Bitmap? {
        if (base64Data.isBlank()) return null
        return try {
            val bytes = Base64.decode(base64Data, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (_: Exception) {
            null
        }
    }

    private fun getCompressFormat(): Bitmap.CompressFormat {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            @Suppress("DEPRECATION")
            Bitmap.CompressFormat.WEBP
        }
    }

    private fun calculateInSampleSize(
        width: Int,
        height: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun scaleToFit(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val ratio = min(
            maxWidth.toFloat() / bitmap.width.toFloat(),
            maxHeight.toFloat() / bitmap.height.toFloat()
        )
        if (ratio >= 1f) return bitmap
        val targetW = max(1, (bitmap.width * ratio).toInt())
        val targetH = max(1, (bitmap.height * ratio).toInt())
        return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
    }

    private fun applyExifOrientation(uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val orientation = context.contentResolver.openInputStream(uri)?.use { stream ->
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
                else -> return bitmap
            }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (_: Exception) {
            bitmap
        }
    }

    private fun sha256Hex(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
}
