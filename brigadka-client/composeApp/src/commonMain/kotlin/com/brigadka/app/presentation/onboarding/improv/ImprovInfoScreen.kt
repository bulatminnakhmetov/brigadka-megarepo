package com.brigadka.app.presentation.onboarding.improv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.brigadka.app.data.api.models.City
import com.brigadka.app.data.api.models.MediaItem
import com.brigadka.app.data.api.models.StringItem
import com.brigadka.app.presentation.profile.common.LoadableValue
import com.brigadka.app.presentation.profile.common.ProfileData
import kotlinx.datetime.LocalDate

@Composable
fun ImprovInfoScreen(component: ImprovInfoComponent) {
    val state by component.profileData.subscribeAsState()
    val improvGoals by component.improvGoals.collectAsState()
    val improvStyles by component.improvStyles.collectAsState()

    ImprovInfoScreen(
        state = state,
        improvGoals = improvGoals,
        improvStyles = improvStyles,
        updateBio = component::updateBio,
        updateGoal = component::updateGoal,
        toggleStyle = component::toggleStyle,
        updateLookingForTeam = component::updateLookingForTeam,
        back = component::back,
        next = component::next,
        isCompleted = component.isCompleted
    )
}

@Composable
fun ImprovInfoScreenPreview() {
    val profileData = ProfileData(
        fullName = "John Doe",
        birthday = LocalDate(2000, 1, 1),
        gender = "male",
        cityId = 1,
        goal = "hobby",
        improvStyles = listOf("shortform"),
        lookingForTeam = true,
        avatar = LoadableValue(
            value = MediaItem(
                id = 1,
                url = "https://example.com/avatar.jpg",
                thumbnail_url = "https://example.com/avatar_thumbnail.jpg"
            )
        ),
        videos = listOf(
            LoadableValue(
                value = MediaItem(
                    id = 0,
                    url = "https://example.com/video1.mp4",
                    thumbnail_url = "https://example.com/video"
                )
            ),
            LoadableValue(
                value = MediaItem(
                    id = 1,
                    url = "https://example.com/video1.mp4",
                    thumbnail_url = "https://example.com/video"
                )
            ),
            LoadableValue(
                value = MediaItem(
                    id = 2,
                    url = "https://example.com/video1.mp4",
                    thumbnail_url = "https://example.com/video"
                )
            ),
        )
    )
    val goals = listOf(
        StringItem(code = "hobby", label = "Hobby"),
        StringItem(code = "professional", label = "Professional")
    )
    val styles = listOf(
        StringItem(code = "shortform", label = "Shortform"),
        StringItem(code = "longform", label = "Longform")
    )

    ImprovInfoScreen(
        state = profileData,
        improvGoals = goals,
        improvStyles = styles,
        updateBio = {},
        updateGoal = {},
        toggleStyle = {},
        updateLookingForTeam = {},
        back = {},
        next = {},
        isCompleted = true
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImprovInfoScreen(
    state: ProfileData,
    improvGoals: List<StringItem>,
    improvStyles: List<StringItem>,
    updateBio: (String) -> Unit,
    updateGoal: (String) -> Unit,
    toggleStyle: (String) -> Unit,
    updateLookingForTeam: (Boolean) -> Unit,
    back: () -> Unit,
    next: () -> Unit,
    isCompleted: Boolean = true,
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
            text = "Расскажите о себе еще",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Эта информация поможет другим импровизаторам лучше вас узнать",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Start,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // TODO: установить ограничения на количество символов
        OutlinedTextField(
            value = state.bio,
            onValueChange = { updateBio(it) },
            label = { Text("О себе") },
            placeholder = { Text("Раскажите больше про ваш опыт...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            shape = MaterialTheme.shapes.medium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Ваша цель в импровизации",
            style = MaterialTheme.typography.titleMedium
        )

        if (improvGoals.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                improvGoals.forEach { goal ->
                    FilterChip(
                        selected = state.goal == goal.code,
                        onClick = { updateGoal(goal.code) },
                        label = { Text(goal.label) }
                    )
                }
            }
        } else {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Что вам нравится в импровизации",
            style = MaterialTheme.typography.titleMedium
        )

        if (improvStyles.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                improvStyles.forEach { style ->
                    FilterChip(
                        selected = style.code in state.improvStyles,
                        onClick = { toggleStyle(style.code) },
                        label = { Text(style.label) }
                    )
                }
            }
        } else {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Ищу команду",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    // TODO: replace newline with proper container sizing
                    text = "Дайте знать другим импровизаторам,что вы\nищете команду",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = state.lookingForTeam,
                onCheckedChange = { updateLookingForTeam(it) }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FilledTonalButton(
                onClick = { back() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Назад")
            }

            Button(
                onClick = { next() },
                modifier = Modifier.weight(1f),
                enabled = isCompleted
            ) {
                Text("Продолжить")
            }
        }
    }
}