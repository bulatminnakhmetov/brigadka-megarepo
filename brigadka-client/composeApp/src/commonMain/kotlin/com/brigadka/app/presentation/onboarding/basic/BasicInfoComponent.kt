package com.brigadka.app.presentation.onboarding.basic

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.brigadka.app.common.coroutineScope
import com.brigadka.app.data.api.models.City
import com.brigadka.app.data.api.models.StringItem
import com.brigadka.app.data.repository.ProfileRepository
import com.brigadka.app.presentation.profile.common.ProfileData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class BasicInfoComponent(
    componentContext: ComponentContext,
    private val _profileData: MutableValue<ProfileData>,
    private val profileRepository: ProfileRepository,
    private val onNext: () -> Unit
) : ComponentContext by componentContext {

    private val coroutineScope = coroutineScope()

    val profileData: Value<ProfileData> = _profileData

    private val _cities = MutableValue<List<City>>(emptyList())
    val cities: Value<List<City>> = _cities

    private val _genders = MutableValue<List<StringItem>>(emptyList())
    val genders: Value<List<StringItem>> = _genders

    val isCompleted: Boolean
        get() = _profileData.value.fullName.isNotEmpty() &&
                _profileData.value.birthday != null &&
                _profileData.value.cityId != null &&
                _profileData.value.gender != null

    init {
        loadCatalogData()
    }

    private fun loadCatalogData() {
        coroutineScope.launch {
            _cities.value = profileRepository.getCities()
            _genders.value = profileRepository.getGenders()
        }
    }

    fun updateFullName(fullName: String) {
        _profileData.update { it.copy(fullName = fullName) }
    }

    fun updateBirthday(birthday: LocalDate?) {
        _profileData.update { it.copy(birthday = birthday) }
    }

    fun updateGender(gender: String) {
        _profileData.update { it.copy(gender = gender) }
    }

    fun updateCityId(cityId: Int) {
        _profileData.update { it.copy(cityId = cityId) }
    }

    fun next() {
        onNext()
    }
}