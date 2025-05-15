package com.brigadka.app.data.repository

import com.brigadka.app.data.api.BrigadkaApiService
import com.brigadka.app.data.api.models.MediaItem
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

interface MediaRepository {
    /**
     * Uploads a media file to the server
     * @param fileBytes The binary content of the file
     * @param fileName The name of the file including extension
     * @param thumbnailBytes Optional thumbnail image bytes
     * @param thumbnailFileName Optional thumbnail file name
     * @return MediaResponse containing the ID and URLs of the uploaded media
     * @throws IOException if there's a network error
     * @throws Exception for other errors
     */
    suspend fun uploadMedia(
        fileBytes: ByteArray,
        fileName: String,
        thumbnailBytes: ByteArray,
        thumbnailFileName: String
    ): MediaItem

    /**
     * Uploads a media file with an automatically generated thumbnail
     * @param fileBytes The binary content of the file
     * @param fileName The name of the file including extension
     * @return MediaResponse containing the ID and URLs of the uploaded media
     * @throws IOException if there's a network error
     * @throws Exception for other errors
     */
    suspend fun uploadMedia(
        fileBytes: ByteArray,
        fileName: String
    ): MediaItem
}

class MediaRepositoryImpl(
    private val api: BrigadkaApiService
) : MediaRepository {

    override suspend fun uploadMedia(
        fileBytes: ByteArray,
        fileName: String,
        thumbnailBytes: ByteArray,
        thumbnailFileName: String
    ): MediaItem =
        withContext(Dispatchers.IO) {
            try {
                api.uploadMedia(
                    file = fileBytes,
                    fileName = fileName,
                    thumbnail = thumbnailBytes,
                    thumbnailFileName = thumbnailFileName
                )
            } catch (e: Exception) {
                // Log the error or handle specific exceptions
                throw e
            }
        }

    override suspend fun uploadMedia(
        fileBytes: ByteArray,
        fileName: String
    ): MediaItem =
        withContext(Dispatchers.Default) {
            val thumbnailData = generateThumbnail(fileBytes, fileName)
            val thumbnailFileName = "thumbnail_${replaceExtensionWithPng(fileName)}"
            withContext(Dispatchers.IO) {
                uploadMedia(fileBytes, fileName, thumbnailData, thumbnailFileName)
            }
        }

    /**
     * Generates a thumbnail from the provided file bytes
     */
    private fun generateThumbnail(fileBytes: ByteArray, fileName: String): ByteArray {
        // This will be platform-specific implementation
        return createThumbnail(fileBytes, fileName.lowercase())
    }
}

fun replaceExtensionWithPng(fileName: String): String {
    val baseName = fileName.substringBeforeLast('.', fileName)
    return "$baseName.png"
}
