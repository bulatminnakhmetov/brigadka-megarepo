package com.brigadka.app.previews

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.brigadka.app.presentation.AppTheme
import com.brigadka.app.presentation.onboarding.basic.BasicInfoScreenPreview
import com.brigadka.app.presentation.onboarding.improv.ImprovInfoScreenPreview

@Preview
@Composable
fun ImprovInfoScreenPreviewPreview() {
    AppTheme {
        Surface {
            ImprovInfoScreenPreview()
        }
    }
}

@Preview
@Composable
fun BasicInfoScreenPreviewPreview() {
    AppTheme {
        Surface {
            BasicInfoScreenPreview()
        }
    }

}

