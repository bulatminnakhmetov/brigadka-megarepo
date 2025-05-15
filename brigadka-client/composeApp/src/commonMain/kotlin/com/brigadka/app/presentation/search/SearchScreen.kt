package com.brigadka.app.presentation.search

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brigadka.app.data.repository.ProfileView
import com.brigadka.app.presentation.profile.common.Avatar
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.brigadka.app.data.api.models.City
import com.brigadka.app.data.repository.SearchResult
import com.brigadka.app.presentation.common.CityPicker
import com.brigadka.app.presentation.common.getProfilesPostfix
import com.brigadka.app.presentation.common.getYearsPostfix


@Composable
fun SearchScreen(component: SearchComponent) {
    val state by component.state.subscribeAsState()

    SearchScreen(
        state = state,
        showFilters = state.showFilters,
        onUpdateAgeRange = component::updateAgeRange,
        onUpdateCityFilter = component::updateCityFilter,
        onToggleGender = component::toggleGender,
        onToggleGoal = component::toggleGoal,
        onToggleImprovStyle = component::toggleImprovStyle,
        onToggleLookingForTeam = component::toggleLookingForTeam,
        onToggleHasAvatar = component::toggleHasAvatar,
        onToggleHasVideo = component::toggleHasVideo,
        onResetFilters = component::resetFilters,
        onPreviousPage = component::previousPage,
        onNextPage = component::nextPage,
        onProfileClick = component::onProfileClick
    )
}

@Composable
fun SearchScreenPreview(showFilters: Boolean) {
    val profiles = listOf(
        ProfileView(
            userID = 1,
            fullName = "John Doe",
            age = 28,
            genderLabel = "Male",
            cityLabel = "New York",
            bio = "Improv enthusiast.",
            goalLabel = "Have fun",
            improvStylesLabels = listOf("Short Form", "Long Form"),
            lookingForTeam = true,
            avatar = null,
            videos = emptyList()
        ),
        ProfileView(
            userID = 1,
            fullName = "Jane Smith",
            age = 32,
            genderLabel = "Female",
            cityLabel = "Los Angeles",
            bio = "Comedy lover and performer.",
            goalLabel = "Meet new people",
            improvStylesLabels = listOf("Musical", "Short Form"),
            lookingForTeam = false,
            avatar = null,
            videos = emptyList()
        ),
        ProfileView(
            userID = 1,
            fullName = "Alex Johnson",
            age = 25,
            genderLabel = "Non-binary",
            cityLabel = "Chicago",
            bio = "Always up for a laugh.",
            goalLabel = "Improve skills",
            improvStylesLabels = listOf("Long Form"),
            lookingForTeam = true,
            avatar = null,
            videos = emptyList()
        )
    )

    val searchResult = SearchResult(
        profiles = profiles,
        page = 1,
        pageSize = 20,
        totalCount = profiles.size
    )

    val improvStyles = listOf(
        Option("short_form", "Короткая форма", false),
        Option("long_form", "Длинная форма", false),
        Option("musical", "Мюзикл", true),
        Option("musical", "Реп", false),
        Option("musical", "Баттлы", true),
        Option("musical", "Плейбек", false),
    )

    val goals = listOf(
        Option("have_fun", "Have Fun", false),
        Option("meet_people", "Meet People", true),
        Option("improve_skills", "Improve Skills", false)
    )

    val genders = listOf(
        Option("male", "Male", false),
        Option("female", "Female", false),
    )

    val state = SearchState(
        searchResult = searchResult,
        isLoading = false,
        improvStyleFilter = improvStyles,
        goalFilter = goals,
        genderFilter = genders,
        lookingForTeamFilter = true,
    )

    SearchScreen(
        state = state,
        showFilters = showFilters,
        onUpdateAgeRange = { _, _ -> },
        onUpdateCityFilter = { },
        onToggleGender = { },
        onToggleGoal = { },
        onToggleImprovStyle = { },
        onToggleLookingForTeam = { },
        onToggleHasAvatar = { },
        onToggleHasVideo = { },
        onResetFilters = { },
        onPreviousPage = { },
        onNextPage = { },
        onProfileClick = { }
    )
}

@Composable
fun SearchScreen(
    state: SearchState,
    showFilters: Boolean,
    onUpdateAgeRange: (min: Int?, max: Int?) -> Unit,
    onUpdateCityFilter: (Int?) -> Unit,
    onToggleGender: (String) -> Unit,
    onToggleGoal: (String) -> Unit,
    onToggleImprovStyle: (String) -> Unit,
    onToggleLookingForTeam: (Boolean) -> Unit,
    onToggleHasAvatar: (Boolean) -> Unit,
    onToggleHasVideo: (Boolean) -> Unit,
    onResetFilters: () -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onProfileClick: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (showFilters) {
            SearchFilters(
                state = state,
                onAgeRangeChange = onUpdateAgeRange,
                onCityChange = onUpdateCityFilter,
                onToggleGender = onToggleGender,
                onToggleGoal = onToggleGoal,
                onToggleImprovStyle = onToggleImprovStyle,
                onLookingForTeamToggle = onToggleLookingForTeam,
                onHasAvatarToggle = onToggleHasAvatar,
                onHasVideoToggle = onToggleHasVideo,
                onReset = onResetFilters,
            )
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.error,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            val searchResults = state.searchResult
            if (searchResults != null) {
                if (searchResults.profiles.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No profiles found matching your criteria",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    Column {
                        Text(
                            text = "Найдено ${searchResults.totalCount} ${getProfilesPostfix(searchResults.totalCount)}",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        LazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {
                            items(searchResults.profiles) { profile ->
                                ProfileCard(profile, onClick = { onProfileClick(profile.userID) })
                            }
                        }

                        // Pagination controls
                        SearchPagination(
                            currentPage = searchResults.page,
                            totalPages = (searchResults.totalCount / searchResults.pageSize) + 1,
                            onPreviousPage = onPreviousPage,
                            onNextPage = onNextPage
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchTopBarPreview() {
    val state = SearchTopBarState(
        query = "",
        onQueryChange = {},
        onSearch = {},
        onToggleFilters = {}
    )
    SearchTopBar(state)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
    state: SearchTopBarState
) {
    TopAppBar(
        title = {
            OutlinedTextField(
                value = state.query,
                onValueChange = state.onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = { Text("Поиск") },
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge,
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            modifier = Modifier.clickable { state.onQueryChange("") }
                        )
                    }
                }
            )
        },
        actions = {
            Row(modifier = Modifier.padding(start = 8.dp)) {
                IconButton(onClick = state.onSearch) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
                IconButton(onClick = state.onToggleFilters) {
                    Icon(Icons.Default.Menu, contentDescription = "Filters")
                }
            }

        }
    )
}

@Composable
fun FilterLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun SearchFilters(
    state: SearchState,
    onAgeRangeChange: (Int?, Int?) -> Unit,
    onCityChange: (Int?) -> Unit,
    onToggleGender: (String) -> Unit,
    onToggleGoal: (String) -> Unit,
    onToggleImprovStyle: (String) -> Unit,
    onLookingForTeamToggle: (Boolean) -> Unit,
    onHasAvatarToggle: (Boolean) -> Unit,
    onHasVideoToggle: (Boolean) -> Unit,
    onReset: () -> Unit
) {
    var minAgeText by remember { mutableStateOf(state.minAgeFilter?.toString() ?: "") }
    var maxAgeText by remember { mutableStateOf(state.maxAgeFilter?.toString() ?: "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Фильтры",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Цель",
            style = MaterialTheme.typography.titleSmall
        )

        ChipsFilter(state.goalFilter, onToggleGoal)

        Spacer(modifier = Modifier.height(16.dp))

        FilterLabel("Пол")

        ChipsFilter(state.genderFilter, onToggleGender)

        Spacer(modifier = Modifier.height(16.dp))

        FilterLabel("Импровизация")

        ChipsFilter(state.improvStyleFilter, onToggleImprovStyle)

        Spacer(modifier = Modifier.height(16.dp))

        FilterLabel("Возраст")

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = minAgeText,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() }) {
                        minAgeText = newValue
                        onAgeRangeChange(newValue.toIntOrNull(), state.maxAgeFilter)
                    }
                },
                modifier = Modifier.weight(1f),
                label = { Text("От") },
                shape = MaterialTheme.shapes.medium
            )

            OutlinedTextField(
                value = maxAgeText,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() }) {
                        maxAgeText = newValue
                        onAgeRangeChange(state.minAgeFilter, newValue.toIntOrNull())
                    }
                },
                modifier = Modifier.weight(1f),
                label = { Text("До") },
                shape = MaterialTheme.shapes.medium
            )
        }

        // City filter
        Text(
            text = "Город",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        DropdownMenu(
            expanded = false,
            onDismissRequest = { },
            modifier = Modifier.fillMaxWidth()
        ) {
            state.cities.forEach { city ->
                DropdownMenuItem(
                    text = { Text(city.name) },
                    onClick = { onCityChange(city.id) }
                )
            }
        }

        CityPicker(cities = state.cities, selectedCityID = state.selectedCityID, onCitySelected = onCityChange)

        Spacer(modifier = Modifier.height(16.dp))

        BooleanFilter("Ищу команду", state.lookingForTeamFilter, onLookingForTeamToggle)
        BooleanFilter("С аватаркой", state.hasAvatarFilter, onHasAvatarToggle)
        BooleanFilter("С видео", state.hasVideoFilter, onHasVideoToggle)

        // Reset button
        Button(
            onClick = onReset,
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
        ) {
            Text("Сбросить")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipsFilter(options: List<Option>, onClick: (String) -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            FilterChip(
                selected = option.isSelected,
                onClick = { onClick(option.id) },
                label = { Text(option.label) },
            )
        }
    }
}

@Composable
fun BooleanFilter(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun ProfileCardPreview() {
    val profile = ProfileView(
        userID = 1,
        fullName = "Олег Сухорослов",
        age = 28,
        genderLabel = "Мужчина",
        cityLabel = "Москва",
        bio = "Improv enthusiast.",
        goalLabel = "Карьера",
        improvStylesLabels = listOf("Short Form", "Long Form"),
        lookingForTeam = true,
        avatar = null,
        videos = emptyList()
    )
    ProfileCard(profile = profile, onClick = {},)
}

@Composable
fun ProfileCard(profile: ProfileView, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Profile avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .padding(end = 16.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Avatar(
                    mediaItem = profile.avatar,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Profile details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = profile.fullName,
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                ) {
                    if (profile.age != null) {
                        Text(
                            text = "${profile.age} ${getYearsPostfix(profile.age)}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }

                    if (profile.cityLabel != null) {
                        Text(
                            text = profile.cityLabel,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

//                if (profile.improvStylesLabels.isNotEmpty()) {
//                    Text(
//                        text = profile.improvStylesLabels.joinToString(", "),
//                        style = MaterialTheme.typography.bodyMedium,
//                        modifier = Modifier.padding(top = 4.dp)
//                    )
//                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (profile.lookingForTeam) {
                        LookingForTeamBadge()
                    }
                    if (profile.goalLabel != null) {
                        GoalBadge(goalLabel = profile.goalLabel)
                    }
                }


            }
        }
    }
}

@Composable
fun LookingForTeamBadge() {
    Box(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "Ищу команду",
            color = Color.Black,
            style = MaterialTheme.typography.labelMedium,
            fontSize = 14.sp
        )
    }
}

@Composable
fun GoalBadge(goalLabel: String) {
    Box(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = goalLabel,
            color = Color.Black,
            style = MaterialTheme.typography.labelMedium,
            fontSize = 14.sp
        )
    }
}

@Composable
fun SearchPagination(
    currentPage: Int,
    totalPages: Int,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit
) {
    // TODO: implement "scroll to load"
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Button(
//            onClick = onPreviousPage,
//            enabled = currentPage > 1
//        ) {
//            Text("Previous")
//        }
//
//        Text("${currentPage} of $totalPages")
//
//        Button(
//            onClick = onNextPage,
//            enabled = currentPage < totalPages
//        ) {
//            Text("Next")
//        }
//    }
}