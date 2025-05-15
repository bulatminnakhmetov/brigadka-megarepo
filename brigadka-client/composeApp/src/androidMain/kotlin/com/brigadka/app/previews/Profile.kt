package com.brigadka.app.previews

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.brigadka.app.presentation.AppTheme
import com.brigadka.app.presentation.profile.view.HomeProfileViewScreenPreview
import com.brigadka.app.presentation.profile.view.OtherProfileViewScreenPreview

@Preview
@Composable
fun HomeProfileViewScreenPreviewPreview() {
    AppTheme {
        Surface {
            HomeProfileViewScreenPreview()
        }
    }

}

@Preview
@Composable
fun OtherProfileViewScreenPreviewPreview() {
    AppTheme {
        Surface {
            OtherProfileViewScreenPreview()
        }
    }
}


