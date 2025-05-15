package com.brigadka.app.presentation.onboarding.improv

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.brigadka.app.common.coroutineScope
import com.brigadka.app.data.api.models.StringItem
import com.brigadka.app.data.repository.ProfileRepository
import com.brigadka.app.presentation.profile.common.ProfileData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ImprovInfoComponent(
    componentContext: ComponentContext,
    private val profileRepository: ProfileRepository,
    private val _profileData: MutableValue<ProfileData>,
    private val onNext: () -> Unit,
    private val onBack: () -> Unit
) : ComponentContext by componentContext {

    private val coroutineScope = coroutineScope()
    
    val profileData: Value<ProfileData> = _profileData

    private val _improvGoals = MutableStateFlow<List<StringItem>>(emptyList())
    val improvGoals: StateFlow<List<StringItem>> = _improvGoals.asStateFlow()

    private val _improvStyles = MutableStateFlow<List<StringItem>>(emptyList())
    val improvStyles: StateFlow<List<StringItem>> = _improvStyles.asStateFlow()

    val isCompleted: Boolean
        get() = profileData.value.improvStyles.isNotEmpty() && profileData.value.bio.isNotEmpty() && profileData.value.goal.isNotEmpty()

    init {
        loadCatalogData()
    }

    private fun loadCatalogData() {
        coroutineScope.launch {
            try {
                _improvGoals.update { profileRepository.getImprovGoals() }
                _improvStyles.update { profileRepository.getImprovStyles() }
            } catch (e: Exception) {
                // TODO: Handle error
            }
        }
    }

    fun updateBio(bio: String) {
        _profileData.update { it.copy(bio = bio) }
    }

    fun updateGoal(goal: String) {
        _profileData.update { it.copy(goal = goal) }
    }

    fun next() {
        onNext()
    }

    fun back() {
        onBack()
    }

    fun updateImprovStyles(styles: List<String>) {
        _profileData.update { it.copy(improvStyles = styles) }
    }

    fun toggleStyle(styleCode: String) {
        val currentStyles = _profileData.value.improvStyles
        val updatedStyles = if (styleCode in currentStyles) {
            currentStyles - styleCode
        } else {
            currentStyles + styleCode
        }
        updateImprovStyles(updatedStyles)
    }

    fun updateLookingForTeam(lookingForTeam: Boolean) {
        _profileData.update { it.copy(lookingForTeam = lookingForTeam) }
    }
}