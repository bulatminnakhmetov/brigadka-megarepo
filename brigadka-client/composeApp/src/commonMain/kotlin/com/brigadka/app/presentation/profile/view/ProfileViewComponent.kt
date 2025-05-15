package com.brigadka.app.presentation.profile.view

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.brigadka.app.common.coroutineScope
import com.brigadka.app.data.api.BrigadkaApiService
import com.brigadka.app.data.repository.ProfileRepository
import com.brigadka.app.presentation.profile.common.LoadableValue
import com.brigadka.app.data.repository.ProfileView
import com.brigadka.app.data.repository.UserDataRepository
import com.brigadka.app.domain.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ProfileViewTopBarState(
    val isCurrentUser: Boolean,
    val onBackClick: () -> Unit,
    val onEditProfile: () -> Unit,
    val onLogout: () -> Unit
)

class ProfileViewComponent(
    componentContext: ComponentContext,
    private val brigadkaApiService: BrigadkaApiService,
    private val userDataRepository: UserDataRepository,
    private val profileRepository: ProfileRepository,
    private val sessionManager: SessionManager,
    private val userID: Int? = null,
    val onEditProfile: (() -> Unit),
    val onContactClick: ((String) -> Unit),
    val onBackClick: () -> Unit
) : ComponentContext by componentContext {

    private val _profileView = MutableValue<LoadableValue<ProfileView>>(LoadableValue(isLoading = true))
    val profileView: Value<LoadableValue<ProfileView>> = _profileView

    private val coroutineScope = coroutineScope()

    val topBarState: ProfileViewTopBarState
        get() = ProfileViewTopBarState(
            isCurrentUser = isEditable,
            onBackClick = onBackClick,
            onEditProfile = onEditProfile,
            onLogout = { coroutineScope.launch { sessionManager.logout() } }
        )

    init {
        coroutineScope.launch {
            val view = profileRepository.getProfileView(userID)
            _profileView.update { it.copy(isLoading = false, value = view) }
        }
    }

    fun onContactClick() {
        if (userID == null) {
            // TODO: log or throw error
            return
        }
        coroutineScope.launch {
            try {
                val chatId = brigadkaApiService.getOrCreateDirectChat(userID).chat_id
                withContext(Dispatchers.Main) {
                    onContactClick.invoke(chatId)
                }
            } catch (e: Exception) {
                // TODO: Handle error (e.g., show a snackbar or log the error)
            }
        }
    }

    val isEditable: Boolean
        get() = userID == null

    val isContactable: Boolean
        get() = userID != null && userID != userDataRepository.requireUserId()
}