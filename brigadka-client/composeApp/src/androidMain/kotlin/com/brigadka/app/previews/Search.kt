package com.brigadka.app.previews

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.brigadka.app.presentation.AppTheme
import com.brigadka.app.presentation.search.ProfileCardPreview
import com.brigadka.app.presentation.search.SearchScreenPreview
import com.brigadka.app.presentation.search.SearchTopBar
import com.brigadka.app.presentation.search.SearchTopBarPreview

@Preview
@Composable
fun SearchTopBarPreviewPreview() {
    AppTheme {
        Surface {
            SearchTopBarPreview()
        }
    }
}

@Preview
@Composable
fun SearchScreenPreviewWithoutFilters() {
    AppTheme {
        Surface {
            SearchScreenPreview(false)
        }
    }
}

@Preview
@Composable
fun SearchProfileCardPreview() {
    AppTheme {
        Surface {
            ProfileCardPreview()
        }
    }

}

@Preview
@Composable
fun SearchScreenPreviewWithFilters() {
    AppTheme {
        Surface {
            SearchScreenPreview(true)
        }
    }
}



