package com.brigadka.app.presentation.common

import androidx.compose.runtime.Composable

interface FilePickerLauncher {
    fun launch()
}

@Composable
expect fun rememberFilePickerLauncher(
    fileType: String,
    onFilePicked: (ByteArray, String) -> Unit,
    onError: (String) -> Unit
): FilePickerLauncher