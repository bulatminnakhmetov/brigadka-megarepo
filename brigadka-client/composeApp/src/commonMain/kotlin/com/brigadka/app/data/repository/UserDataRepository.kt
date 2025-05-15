// UserDataRepository.kt
package com.brigadka.app.data.repository

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


interface UserDataRepository {
    val isLoggedIn: Boolean
    suspend fun setCurrentUserId(userId: Int)
    suspend fun clearCurrentUserId()
    fun requireUserId(): Int
}

class UserDataRepositoryImpl(
    private val settings: Settings,
) : UserDataRepository {
    private val userIdKey = "user_id"

    override val isLoggedIn: Boolean = getStoredUserId() != null

    override fun requireUserId(): Int {
        return getStoredUserId()!! // TODO: Handle null case
    }

    override suspend fun setCurrentUserId(userId: Int) {
        settings[userIdKey] = userId
    }

    override suspend fun clearCurrentUserId() {
        settings.remove(userIdKey)
    }

    private fun getStoredUserId(): Int? {
        return settings.getIntOrNull(userIdKey)
    }
}