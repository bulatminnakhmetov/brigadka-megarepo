package com.brigadka.app.data.repository

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import kotlinx.cinterop.usePinned
import platform.AVFoundation.AVAsset
import platform.AVFoundation.AVURLAsset
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.dataWithBytes
import platform.Foundation.getBytes
import platform.Foundation.writeToFile
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePNGRepresentation
import platform.darwin.NSObject
import platform.Foundation.*
import platform.AVFoundation.*
import platform.CoreGraphics.*
import platform.CoreMedia.*
import platform.UIKit.*
import kotlinx.cinterop.*

private const val THUMBNAIL_SIZE = 256.0

actual fun createThumbnail(fileBytes: ByteArray, fileName: String): ByteArray {
    return when {
        fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") -> {
            generateImageThumbnail(fileBytes)
        }
        fileName.endsWith(".mp4") || fileName.endsWith(".mov") -> {
            generateVideoThumbnail(fileBytes, fileName)
        }
        else -> {
            fileBytes // Return original if can't generate thumbnail
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun generateImageThumbnail(imageBytes: ByteArray): ByteArray {
    val nsData = imageBytes.usePinned {
        NSData.dataWithBytes(it.addressOf(0), imageBytes.size.toULong())
    }
    val originalImage = UIImage.imageWithData(nsData) ?: return ByteArray(0)

    // Calculate scale to maintain aspect ratio
    val originalWidth = originalImage.size.useContents { width }
    val originalHeight = originalImage.size.useContents { height }
    val scale = THUMBNAIL_SIZE / maxOf(originalWidth, originalHeight)

    val thumbnailWidth = originalWidth * scale
    val thumbnailHeight = originalHeight * scale

    UIGraphicsBeginImageContextWithOptions(CGSizeMake(thumbnailWidth, thumbnailHeight), false, 1.0)
    originalImage.drawInRect(CGRectMake(0.0, 0.0, thumbnailWidth, thumbnailHeight))
    val thumbnailImage = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()

    val thumbnailData = thumbnailImage?.let { UIImagePNGRepresentation(thumbnailImage) } ?: return ByteArray(0)

    // Convert NSData back to ByteArray
    val length = thumbnailData.length.toInt()
    val result = ByteArray(length)
    result.usePinned { pinned ->
        thumbnailData.getBytes(pinned.addressOf(0), length.toULong())
    }

    return result
}
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun generateVideoThumbnail(videoBytes: ByteArray, fileName: String): ByteArray {
    // Save video to temporary file
    val tempFile = NSTemporaryDirectory() + "/temp_video_$fileName"
    val nsData = videoBytes.usePinned { pinned ->
        NSData.dataWithBytes(pinned.addressOf(0), videoBytes.size.toULong())
    }

    if (!nsData.writeToFile(tempFile, true)) {
        return ByteArray(0)
    }

    val url = NSURL.fileURLWithPath(tempFile)
    val asset = AVAsset.assetWithURL(url)
    val imageGenerator = AVAssetImageGenerator(asset)
    imageGenerator.maximumSize = CGSizeMake(THUMBNAIL_SIZE, THUMBNAIL_SIZE)

    val cgImage = memScoped {
        val requestedTime = CMTimeMake(1, 1)
        val actualTimePtr = alloc<CMTime>()
        val errorPtr = alloc<ObjCObjectVar<NSError?>>()

        val cgImageRef = imageGenerator.copyCGImageAtTime(
            requestedTime,
            actualTimePtr.ptr,
            errorPtr.ptr
        )

        if (errorPtr.value != null || cgImageRef == null) {
            return ByteArray(0)
        }

        cgImageRef // ✅ Safe to return; it's a retained CoreGraphics object
    }

    // Generate UIImage and JPEG thumbnail
    val thumbnailImage = UIImage(cgImage)
    val thumbnailData = UIImageJPEGRepresentation(thumbnailImage, 0.8) ?: return ByteArray(0)

    val length = thumbnailData.length.toInt()
    val result = ByteArray(length)

    result.usePinned { pinned ->
        thumbnailData.getBytes(pinned.addressOf(0), length.toULong())
    }

    // Clean up
    NSFileManager.defaultManager.removeItemAtPath(tempFile, null)

    return result
}