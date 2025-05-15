package com.brigadka.app.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIModalPresentationFormSheet
import platform.darwin.NSObject
import platform.posix.memcpy
import kotlin.native.ref.WeakReference

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Composable
actual fun rememberFilePickerLauncher(
    fileType: String,
    onFilePicked: (ByteArray, String) -> Unit,
    onError: (String) -> Unit
): FilePickerLauncher {
    val uiViewController = UIViewController.currentViewController()

    // Parse file type to UTI
    val uti = when {
        fileType == "image/*" -> listOf("public.image")
        fileType == "video/*" -> listOf("public.movie")
        fileType.contains("/*") -> {
            val mainType = fileType.split("/")[0]
            listOf("public.$mainType")
        }
        else -> listOf(fileType)
    }

    val delegate = remember {
        DocumentPickerDelegate(
            onFilePicked = { data, fileName ->
                onFilePicked(data, fileName)
            },
            onError = { error ->
                onError(error)
            }
        )
    }

    DisposableEffect(uiViewController) {
        onDispose {
            // Cleanup if needed
        }
    }

    return remember {
        object : FilePickerLauncher {
            override fun launch() {
                val documentPicker = UIDocumentPickerViewController(
                    documentTypes = uti,
                    inMode = UIDocumentPickerMode.UIDocumentPickerModeImport
                )
                documentPicker.delegate = delegate
                documentPicker.modalPresentationStyle = UIModalPresentationFormSheet
                uiViewController.presentViewController(documentPicker, true, null)
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private class DocumentPickerDelegate(
    private val onFilePicked: (ByteArray, String) -> Unit,
    private val onError: (String) -> Unit
) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>
    ) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL ?: run {
            onError("No document selected")
            return
        }

        try {
            val fileName = url.lastPathComponent ?: "unknown_file"
            val data = NSData.Companion.dataWithContentsOfURL(url) ?: run {
                onError("Could not read file data")
                return
            }

            val bytes = ByteArray(data.length.toInt())
            bytes.usePinned { pinnedBytes ->
                memcpy(pinnedBytes.addressOf(0), data.bytes, data.length)
            }

            onFilePicked(bytes, fileName)
        } catch (e: Exception) {
            onError("Error reading file: ${e.message}")
        }
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        // User canceled, do nothing
    }
}

// Extension to get current UIViewController
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
fun UIViewController.Companion.currentViewController(): UIViewController {
    val keyWindow = UIApplication.sharedApplication.keyWindow
    var viewController = keyWindow?.rootViewController

    while (viewController?.presentedViewController != null) {
        viewController = viewController.presentedViewController
    }

    return viewController!!
}