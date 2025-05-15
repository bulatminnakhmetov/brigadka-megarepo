package com.brigadka.app.presentation.common

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

@Composable
actual fun rememberFilePickerLauncher(
    fileType: String,
    onFilePicked: (ByteArray, String) -> Unit,
    onError: (String) -> Unit
): FilePickerLauncher {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val fileName = getFileName(context, uri) ?: "unknown_file"
                val bytes = readBytes(context, uri)
                onFilePicked(bytes, fileName)
            } catch (e: Exception) {
                onError("Failed to read file: ${e.message}")
            }
        }
    }

    return remember {
        object : FilePickerLauncher {
            override fun launch() {
                launcher.launch(fileType)
            }
        }
    }
}

private fun getFileName(context: Context, uri: Uri): String? {
    val projection = arrayOf("_display_name")
    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndexOrThrow("_display_name")
            return cursor.getString(columnIndex)
        }
    }
    return uri.lastPathSegment
}

private fun readBytes(context: Context, uri: Uri): ByteArray {
    val inputStream: InputStream = context.contentResolver.openInputStream(uri)
        ?: throw IllegalArgumentException("Cannot open input stream for URI: $uri")

    return inputStream.use { input ->
        val buffer = ByteArray(8192)
        val output = ByteArrayOutputStream()

        var read: Int
        while (input.read(buffer).also { read = it } != -1) {
            output.write(buffer, 0, read)
        }

        output.toByteArray()
    }
}