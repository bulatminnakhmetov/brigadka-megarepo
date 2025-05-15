package com.brigadka.app.presentation.onboarding.basic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.brigadka.app.data.api.models.City
import com.brigadka.app.data.api.models.MediaItem
import com.brigadka.app.data.api.models.StringItem
import com.brigadka.app.presentation.common.CityPicker
import com.brigadka.app.presentation.common.DatePickerField
import com.brigadka.app.presentation.profile.common.LoadableValue
import com.brigadka.app.presentation.profile.common.ProfileData
import kotlinx.datetime.LocalDate


@Composable
fun BasicInfoScreen(component: BasicInfoComponent) {
    val state by component.profileData.subscribeAsState()
    val cities by component.cities.subscribeAsState()
    val genders by component.genders.subscribeAsState()
    BasicInfoScreen(
        profileData = state,
        cities = cities,
        genders = genders,
        updateFullName = component::updateFullName,
        updateBirthday = component::updateBirthday,
        updateGender = component::updateGender,
        updateCityId = component::updateCityId,
        next = component::next,
        isCompleted = component.isCompleted
    )
}

@Composable
fun BasicInfoScreenPreview() {
    val profileData = ProfileData(
        fullName = "John Doe",
        birthday = LocalDate(2000, 1, 1),
        gender = "male",
        cityId = 1,
        bio = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
        goal = "hobby",
        improvStyles = listOf("shortform"),
        lookingForTeam = true,
        avatar = LoadableValue(value = MediaItem(
            id = 1,
            url = "https://example.com/avatar.jpg",
            thumbnail_url = "https://example.com/avatar_thumbnail.jpg"
        )),
        videos = listOf(
            LoadableValue(value = MediaItem(id = 0, url = "https://example.com/video1.mp4", thumbnail_url = "https://example.com/video")),
            LoadableValue(value = MediaItem(id = 1, url = "https://example.com/video1.mp4", thumbnail_url = "https://example.com/video")),
            LoadableValue(value = MediaItem(id = 2, url = "https://example.com/video1.mp4", thumbnail_url = "https://example.com/video")),
        )
    )
    val cities = listOf(
        City(id = 1, name = "New York"),
        City(id = 2, name = "Los Angeles"),
        City(id = 3, name = "Chicago")
    )
    val genders = listOf(
        StringItem(code = "male", label = "Male"),
        StringItem(code = "female", label = "Female")
    )

    BasicInfoScreen(
        profileData = profileData,
        cities = cities,
        genders = genders,
        updateFullName = {},
        updateBirthday = {},
        updateGender = {},
        updateCityId = {},
        next = {},
        isCompleted = true,
    )
}

@Composable
fun BasicInfoScreen(
    profileData: ProfileData,
    cities: List<City>,
    genders: List<StringItem>,
    updateFullName: (String) -> Unit,
    updateBirthday: (LocalDate?) -> Unit,
    updateGender: (String) -> Unit,
    updateCityId: (Int) -> Unit,
    next: () -> Unit,
    isCompleted: Boolean
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Расскажите о себе",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = profileData.fullName,
            onValueChange = { updateFullName(it) },
            label = { Text("Имя") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
        )

        DatePickerField(
            label = "День рождения",
            onDateSelected = {
                updateBirthday(it)
            },
        )

        // Gender selection from API

        if (genders.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically) {
                Text("Пол")

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    genders.forEach { gender ->
                        FilterChip(
                            selected = profileData.gender == gender.code,
                            onClick = { updateGender(gender.code) },
                            label = { Text(gender.label) }
                        )
                    }
                }
            }



        } else {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }

        // City selection with dropdown
        CityPicker(cities, profileData.cityId, onCitySelected = { cityID ->
            updateCityId(cityID)
        })

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = next,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            enabled = isCompleted
        ) {
            Text("Продолжить")
        }
    }
}