package com.brigadka.app.data.repository

import co.touchlab.kermit.Logger
import com.brigadka.app.data.api.BrigadkaApiServiceAuthorized
import com.brigadka.app.data.api.models.City
import com.brigadka.app.data.api.models.MediaItem
import com.brigadka.app.data.api.models.Profile
import com.brigadka.app.data.api.models.ProfileCreateRequest
import com.brigadka.app.data.api.models.ProfileUpdateRequest
import com.brigadka.app.data.api.models.SearchRequest
import com.brigadka.app.data.api.models.StringItem
import com.brigadka.app.domain.session.LoggingState
import com.brigadka.app.domain.session.SessionManager
import com.brigadka.app.presentation.profile.common.LoadableValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private val logger = Logger.withTag("ProfileRepository")

interface ProfileRepository {
    val currentUserProfile: StateFlow<LoadableValue<ProfileView>>

    // Get any profile by ID without changing currentUserProfile
    suspend fun getProfileView(userId: Int?): ProfileView

    // Update the current user's profile
    suspend fun updateProfile(request: ProfileUpdateRequest)

    // Create a new profile for the current user
    suspend fun createProfile(request: ProfileCreateRequest)

    // Additional methods for reference data
    suspend fun getCities(): List<City>
    suspend fun getGenders(): List<StringItem>
    suspend fun getImprovGoals(): List<StringItem>
    suspend fun getImprovStyles(): List<StringItem>

    // Search profiles
    suspend fun searchProfiles(request: SearchRequest): SearchResult
    suspend fun searchProfiles(
        fullName: String? = null,
        ageMin: Int? = null,
        ageMax: Int? = null,
        cityId: Int? = null,
        genders: List<String>? = null,
        goals: List<String>? = null,
        improvStyles: List<String>? = null,
        lookingForTeam: Boolean? = null,
        hasAvatar: Boolean? = null,
        hasVideo: Boolean? = null,
        page: Int = 1,
        pageSize: Int = 20
    ): SearchResult
}

class ProfileRepositoryImpl(
    coroutineScope: CoroutineScope,
    private val apiService: BrigadkaApiServiceAuthorized,
    private val sessionManager: SessionManager,
    private val userDataRepository: UserDataRepository,
) : ProfileRepository {

    private val _currentUserProfile = MutableStateFlow<LoadableValue<ProfileView>>(LoadableValue(isLoading = false))
    override val currentUserProfile: StateFlow<LoadableValue<ProfileView>> = _currentUserProfile

    init {
        coroutineScope.launch {
            val currentState = sessionManager.loggingState.value
            if (currentState is LoggingState.LoggedIn) {
                loadUserProfile()
            }
            sessionManager.loggingState.collect { loggingState ->
                if (loggingState is LoggingState.LoggedIn) {
                    loadUserProfile()
                }
            }
        }
    }

    suspend fun loadUserProfile() {
        _currentUserProfile.update { it.copy(isLoading = true) }
        try {
            val userID = userDataRepository.requireUserId()
            val profile = withContext(Dispatchers.IO) {
                apiService.getProfile(userID)
            }
            _currentUserProfile.update { it.copy(isLoading = false, value = convertToProfileView(profile)) }
        } catch (e: Exception) {
            _currentUserProfile.update { it.copy(isLoading = false) }
            logger.e("Failed to load user profile: ${e.message}")
            // TODO: handler error
        }
    }

    override suspend fun getProfileView(userId: Int?): ProfileView {
        val profile = withContext(Dispatchers.IO) {
            apiService.getProfile(userId ?: userDataRepository.requireUserId())
        }
        return convertToProfileView(profile)
    }

    override suspend fun createProfile(request: ProfileCreateRequest) {
        withContext(Dispatchers.IO) {
            apiService.createProfile(request)
        }
    }

    override suspend fun updateProfile(request: ProfileUpdateRequest){
        withContext(Dispatchers.IO) {
            apiService.updateProfile(request)
        }
    }

    override suspend fun getCities(): List<City> {
        return withContext(Dispatchers.IO) {
            apiService.getCities()
        }
    }

    override suspend fun getGenders(): List<StringItem> {
        return withContext(Dispatchers.IO) {
            apiService.getGenders()
        }
    }

    override suspend fun getImprovGoals(): List<StringItem> {
        return withContext(Dispatchers.IO) {
            apiService.getImprovGoals()
        }
    }

    override suspend fun getImprovStyles(): List<StringItem> {
        return withContext(Dispatchers.IO) {
            apiService.getImprovStyles()
        }
    }

    override suspend fun searchProfiles(
        fullName: String?,
        ageMin: Int?,
        ageMax: Int?,
        cityId: Int?,
        genders: List<String>?,
        goals: List<String>?,
        improvStyles: List<String>?,
        lookingForTeam: Boolean?,
        hasAvatar: Boolean?,
        hasVideo: Boolean?,
        page: Int,
        pageSize: Int
    ): SearchResult {
        val request = SearchRequest(
            full_name = fullName,
            age_min = ageMin,
            age_max = ageMax,
            city_id = cityId,
            genders = genders,
            goals = goals,
            improv_styles = improvStyles,
            looking_for_team = lookingForTeam,
            has_avatar = hasAvatar,
            has_video = hasVideo,
            page = page,
            page_size = pageSize
        )

        return searchProfiles(request)
    }

    override suspend fun searchProfiles(request: SearchRequest): SearchResult {
        val response = apiService.searchProfiles(request)

        val profileViews = response.profiles.map { profile ->
            convertToProfileView(profile)
        }

        return SearchResult(
            profiles = profileViews,
            page = response.page,
            pageSize = response.page_size,
            totalCount = response.total_count
        )
    }


    private suspend fun convertToProfileView(profile: Profile): ProfileView {
        val cities = getCities()
        val improvGoals = getImprovGoals()
        val improvStyles = getImprovStyles()
        val genders = getGenders()

        return ProfileView(
            userID = profile.user_id,
            fullName = profile.full_name,
            age = calculateAge(profile.birthday),
            genderLabel = genders.find { it.code == profile.gender }?.label,
            cityLabel = cities.find { it.id == profile.city_id }?.name,
            bio = profile.bio,
            goalLabel = improvGoals.find { it.code == profile.goal }?.label,
            improvStylesLabels = profile.improv_styles.mapNotNull { styleCode ->
                improvStyles.find { it.code == styleCode }?.label
            },
            lookingForTeam = profile.looking_for_team,
            avatar = profile.avatar,
            videos = profile.videos
        )
    }
}


data class ProfileView(
    val userID: Int,
    val fullName: String,
    val age: Int?,
    val genderLabel: String?,
    val cityLabel: String?,
    val bio: String,
    val goalLabel: String?,
    val improvStylesLabels: List<String> = emptyList(),
    val lookingForTeam: Boolean = false,
    val avatar: MediaItem?,
    val videos: List<MediaItem> = emptyList()
)

data class SearchResult(
    val profiles: List<ProfileView>,
    val page: Int,
    val pageSize: Int,
    val totalCount: Int
)

fun calculateAge(birthday: LocalDate): Int {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    var age = today.year - birthday.year
    if (
        today.monthNumber < birthday.monthNumber ||
        (today.monthNumber == birthday.monthNumber && today.dayOfMonth < birthday.dayOfMonth)
    ) {
        age--
    }
    return age
}