package com.brigadka.app.presentation.onboarding.media

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.brigadka.app.common.coroutineScope
import com.brigadka.app.data.repository.MediaRepository
import com.brigadka.app.presentation.profile.common.ProfileData
import com.brigadka.app.presentation.profile.common.LoadableValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MediaUploadComponent(
    componentContext: ComponentContext,
    private val mediaRepository: MediaRepository,
    private val _profileData: MutableValue<ProfileData>,
    private val onFinish: () -> Unit,
    private val onBack: () -> Unit,
) : ComponentContext by componentContext {

    private val scope = coroutineScope()

    val profileData: Value<ProfileData> = _profileData

    fun removeVideo(id: Int?) {
        if (id == null) {
            // TODO: when removing uploading video couroutine should be cancelled
            _profileData.update {
                val idx = it.videos.indexOfFirst { video -> video.isLoading }
                it.copy(videos = it.videos.filterIndexed { i, _ -> i != idx })
            }
        } else {
            _profileData.update {
                it.copy(videos = it.videos.filter { video -> video.value?.id != id })
            }
        }
    }

    fun removeAvatar() {
        _profileData.update {
            it.copy(avatar = LoadableValue())
        }
    }

    fun uploadAvatar(fileBytes: ByteArray, fileName: String) {
        _profileData.update { it.copy(avatar = it.avatar.copy(isLoading = true)) }

        scope.launch {
            try {
                val mediaItem = mediaRepository.uploadMedia(fileBytes, fileName)
                _profileData.update { it.copy(avatar = LoadableValue(mediaItem)) }
            } catch (e: Exception) {
                _profileData.update { it.copy(avatar = it.avatar.copy(isLoading = false)) }
                // TODO: log exception
            }
        }
    }

    fun uploadVideo(fileBytes: ByteArray, fileName: String) {
        _profileData.update { it.copy(videos = it.videos + LoadableValue(isLoading = true)) }

        scope.launch {
            try {
                val mediaItem = mediaRepository.uploadMedia(fileBytes, fileName)

                _profileData.update { it ->
                    val idx = it.videos.indexOfFirst { it.isLoading }
                    it.copy(videos = it.videos.mapIndexed() { i, item ->
                        if (i == idx) LoadableValue(mediaItem) else item
                    })
                }

            } catch (e: Exception) {
                // TODO: log exception
                _profileData.update {
                    val idx = it.videos.indexOfFirst { it.isLoading }
                    it.copy(videos = it.videos.filterIndexed { i, _ -> i != idx})
                }
            }
        }
    }

    fun finish() {
        onFinish()
    }

    fun back() {
        onBack()
    }
}
