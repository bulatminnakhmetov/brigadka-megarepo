package com.brigadka.app.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaDataSource
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

private const val THUMBNAIL_SIZE = 256

actual fun createThumbnail(fileBytes: ByteArray, fileName: String): ByteArray {
    return when {
        fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") -> {
            generateImageThumbnail(fileBytes)
        }
        fileName.endsWith(".mp4") || fileName.endsWith(".mov") ||
                fileName.endsWith(".webm") || fileName.endsWith(".avi") -> {
            generateVideoThumbnail(fileBytes)
        }
        else -> {
            fileBytes // Return original if can't generate thumbnail
        }
    }
}

private fun generateImageThumbnail(imageBytes: ByteArray): ByteArray {
    val originalBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    val thumbnailBitmap = ThumbnailUtils.extractThumbnail(
        originalBitmap,
        THUMBNAIL_SIZE,
        THUMBNAIL_SIZE
    )

    val outputStream = ByteArrayOutputStream()
    thumbnailBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    val result = outputStream.toByteArray()

    originalBitmap.recycle()
    thumbnailBitmap.recycle()

    return result
}

private fun generateVideoThumbnail(videoBytes: ByteArray): ByteArray {
    val retriever = MediaMetadataRetriever()
    try {
        retriever.setDataSource(object : MediaDataSource() {
            override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
                if (position >= videoBytes.size) return -1
                val length = minOf(size, videoBytes.size - position.toInt())
                System.arraycopy(videoBytes, position.toInt(), buffer, offset, length)
                return length
            }

            override fun getSize(): Long = videoBytes.size.toLong()
            override fun close() {}
        })

        val bitmap = retriever.frameAtTime
        // Convert Bitmap to ByteArray (e.g., PNG or JPEG)
        val stream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    } catch (e: Exception) {
        e.printStackTrace()
        return ByteArray(0)
    } finally {
        retriever.release()
    }
}