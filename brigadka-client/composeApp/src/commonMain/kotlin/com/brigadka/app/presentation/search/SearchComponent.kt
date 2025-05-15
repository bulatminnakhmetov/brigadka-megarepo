package com.brigadka.app.presentation.search

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.brigadka.app.common.coroutineScope
import com.brigadka.app.data.api.models.City
import com.brigadka.app.data.api.models.StringItem
import com.brigadka.app.data.repository.ProfileRepository
import com.brigadka.app.data.repository.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SearchTopBarState(
    val query: String,
    val onQueryChange: (String) -> Unit,
    val onSearch: () -> Unit,
    val onToggleFilters: () -> Unit
)

class SearchComponent(
    componentContext: ComponentContext,
    private val profileRepository: ProfileRepository,
    val onProfileClickCallback: (Int) -> Unit
) : ComponentContext by componentContext {

    private val _state = MutableValue(SearchState())
    val state: Value<SearchState> get() = _state

    val topBarState: SearchTopBarState
        get() = SearchTopBarState(
            query = _state.value.nameFilter ?: "",
            onQueryChange = ::updateNameFilter,
            onSearch = ::performSearch,
            onToggleFilters = ::toggleFilters
        )

    private val coroutineScope = coroutineScope()
    private var searchJob: Job? = null

    fun toggleFilters() {
        _state.update { it.copy(showFilters = !it.showFilters) }
    }

    init {
        loadReferenceData()
        performSearch()
    }

    private fun loadReferenceData() {
        coroutineScope.launch {
            try {
                val cities = profileRepository.getCities()
                val genders = profileRepository.getGenders()
                val goals = profileRepository.getImprovGoals()
                val styles = profileRepository.getImprovStyles()

                _state.update { it.copy(
                    cities = cities,
                    genderFilter = genders.toOptions(),
                    goalFilter = goals.toOptions(),
                    improvStyleFilter = styles.toOptions(),
                    isLoading = false
                ) }

            } catch (e: Exception) {
                _state.update { it.copy(
                    error = "Failed to load reference data", // TODO: gracefully degrade
                    isLoading = false
                ) }
            }
        }
    }

    fun performSearch() {
        val currentState = _state.value

        // Cancel previous search if still running
        searchJob?.cancel()

        _state.update { it.copy(isLoading = true, error = null) }

        searchJob = coroutineScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    profileRepository.searchProfiles(
                        fullName = currentState.nameFilter,
                        ageMin = currentState.minAgeFilter,
                        ageMax = currentState.maxAgeFilter,
                        cityId = currentState.selectedCityID,
                        genders = currentState.genderFilter.mapNotNull { if (it.isSelected) it.id else null },
                        goals = currentState.goalFilter.mapNotNull { if (it.isSelected) it.id else null },
                        improvStyles = currentState.improvStyleFilter.mapNotNull { if (it.isSelected) it.id else null },
                        lookingForTeam = if (currentState.lookingForTeamFilter) true else null,
                        hasAvatar = if (currentState.hasAvatarFilter) true else null,
                        hasVideo = if (currentState.hasVideoFilter) true else null,
                        page = currentState.currentPage,
                        pageSize = currentState.pageSize
                    )
                }

                _state.update { it.copy(
                    searchResult = result,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _state.update { it.copy(
                    error = "Search failed: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }

    fun updateNameFilter(name: String) {
        _state.update { it.copy(nameFilter = name) }
    }

    fun updateAgeRange(min: Int?, max: Int?) {
        _state.update { it.copy(
            minAgeFilter = min,
            maxAgeFilter = max
        ) }
    }

    fun updateCityFilter(cityId: Int?) {
        _state.update { it.copy(selectedCityID = cityId) }
    }

    fun toggleGender(gender: String) {
        _state.update { it.copy(genderFilter = it.genderFilter.toggle(gender)) }
    }

    fun toggleGoal(goal: String) {
        _state.update { it.copy(goalFilter = it.goalFilter.toggle(goal)) }
    }

    fun toggleImprovStyle(style: String) {
        _state.update {it.copy(improvStyleFilter = it.improvStyleFilter.toggle(style))}
    }

    fun toggleLookingForTeam(value: Boolean) {
        _state.update { it.copy(lookingForTeamFilter = value) }
    }

    fun toggleHasAvatar(value: Boolean) {
        _state.update { it.copy(hasAvatarFilter = value) }
    }

    fun toggleHasVideo(value: Boolean) {
        _state.update { it.copy(hasVideoFilter = value) }
    }

    fun nextPage() {
        val currentResults = _state.value.searchResult
        if (currentResults != null &&
            currentResults.page < (currentResults.totalCount / currentResults.pageSize) + 1) {
            _state.update { it.copy(currentPage = it.currentPage + 1) }
            performSearch()
        }
    }

    fun previousPage() {
        if (_state.value.currentPage > 1) {
            _state.update { it.copy(currentPage = it.currentPage - 1) }
            performSearch()
        }
    }

    fun onProfileClick(userId: Int) {
        onProfileClickCallback(userId)
    }

    fun resetFilters() {
        _state.update {
            SearchState(
                cities = it.cities,
                genderFilter = it.genderFilter.reset(),
                goalFilter = it.goalFilter.reset(),
                improvStyleFilter = it.improvStyleFilter.reset(),
            )
        }
        performSearch()
    }
}

fun List<Option>.toggle(
    optionId: String
): List<Option> {
    return map { option ->
        if (option.id == optionId) {
            option.copy(isSelected = !option.isSelected)
        } else {
            option
        }
    }
}

fun List<Option>.reset(): List<Option> {
    return map { option -> option.copy(isSelected = false) }
}

fun List<StringItem>.toOptions(): List<Option> {
    return map { item -> Option(id = item.code, label = item.label, isSelected = false) }
}

data class Option (
    val id: String,
    val label: String,
    val isSelected: Boolean,
)

data class SearchState(
    // Reference data
    val cities: List<City> = emptyList(),

    val showFilters: Boolean = false,

    // Filter values
    val nameFilter: String? = null,

    val minAgeFilter: Int? = null,
    val maxAgeFilter: Int? = null,

    val genderFilter: List<Option> = emptyList(),
    val goalFilter: List<Option> = emptyList(),
    val improvStyleFilter: List<Option> = emptyList(),

    val selectedCityID: Int? = null,

    val lookingForTeamFilter: Boolean = false,
    val hasAvatarFilter: Boolean = false,
    val hasVideoFilter: Boolean = false,

    // Pagination
    val currentPage: Int = 1,
    val pageSize: Int = 20,

    // Results
    val searchResult: SearchResult? = null,

    // UI state
    val isLoading: Boolean = true,
    val error: String? = null
)