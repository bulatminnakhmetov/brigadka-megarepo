package com.brigadka.app.data.api

import com.brigadka.app.data.api.models.*
import com.brigadka.app.data.repository.Token
import com.brigadka.app.data.repository.AuthTokenRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

interface BrigadkaApiServiceUnauthorized {
    suspend fun login(request: LoginRequest): AuthResponse
    suspend fun register(request: RegisterRequest): AuthResponse
    suspend fun refreshToken(refreshToken: String): AuthResponse
    suspend fun verifyToken(token: String): String
}

interface BrigadkaApiServiceAuthorized {
    // Catalog endpoints
    suspend fun getCities(): List<City>
    suspend fun getGenders(lang: String? = null): List<StringItem>
    suspend fun getImprovGoals(lang: String? = null): List<StringItem>
    suspend fun getImprovStyles(lang: String? = null): List<StringItem>

    // Profile endpoints
    suspend fun createProfile(request: ProfileCreateRequest): Profile
    suspend fun updateProfile(request: ProfileUpdateRequest): Profile
    suspend fun getProfile(userId: Int): Profile

    // Media endpoints
    suspend fun uploadMedia(file: ByteArray, fileName: String, thumbnail: ByteArray, thumbnailFileName: String): MediaItem

    // Search endpoints
    suspend fun searchProfiles(request: SearchRequest): SearchResponse

    // Chat endpoints
    suspend fun getOrCreateDirectChat(userID: Int): ChatIDResponse
    suspend fun getChats(): List<Chat>
    suspend fun getChat(chatId: String): Chat
    suspend fun getChatMessages(chatId: String, limit: Int? = null, offset: Int? = null): List<ChatMessage>
    suspend fun sendMessage(chatId: String, request: SendMessageRequest): ChatMessage
    suspend fun addParticipant(chatId: String, request: AddParticipantRequest): String
    suspend fun removeParticipant(chatId: String, userId: Int): String
    suspend fun addReaction(messageId: String, request: AddReactionRequest): Map<String, String>
    suspend fun removeReaction(messageId: String, reactionCode: String): Map<String, String>
    
    // Push endpoints
    suspend fun registerPushToken(request: RegisterPushTokenRequest): Map<String, String>
    suspend fun unregisterPushToken(request: UnregisterPushTokenRequest): Map<String, String>
}

interface BrigadkaApiService : BrigadkaApiServiceUnauthorized, BrigadkaApiServiceAuthorized

class BrigadkaApiServiceUnauthorizedImpl(
    private val client: HttpClient,
    private val baseUrl: String
) : BrigadkaApiServiceUnauthorized {

    override suspend fun login(request: LoginRequest): AuthResponse {
        return client.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun register(request: RegisterRequest): AuthResponse {
        return client.post("$baseUrl/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun refreshToken(refreshToken: String): AuthResponse {
        return client.post("$baseUrl/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshRequest(refresh_token = refreshToken))
        }.body()
    }

    override suspend fun verifyToken(token: String): String {
        return client.get("$baseUrl/api/auth/verify") {
            header("Authorization", "Bearer $token")
        }.body()
    }
}

class BrigadkaApiServiceAuthorizedImpl(
    private val client: HttpClient,
    private val baseUrl: String
) : BrigadkaApiServiceAuthorized {

    override suspend fun getCities(): List<City> {
        return client.get("$baseUrl/profiles/catalog/cities").body()
    }

    override suspend fun getGenders(lang: String?): List<StringItem> {
        return client.get("$baseUrl/profiles/catalog/genders") {
            if (lang != null) {
                parameter("lang", lang)
            }
        }.body()
    }

    override suspend fun getImprovGoals(lang: String?): List<StringItem> {
        return client.get("$baseUrl/profiles/catalog/improv-goals") {
            if (lang != null) {
                parameter("lang", lang)
            }
        }.body()
    }

    override suspend fun getImprovStyles(lang: String?): List<StringItem> {
        return client.get("$baseUrl/profiles/catalog/improv-styles") {
            if (lang != null) {
                parameter("lang", lang)
            }
        }.body()
    }

    override suspend fun createProfile(request: ProfileCreateRequest): Profile {
        return client.post("$baseUrl/profiles") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun updateProfile(request: ProfileUpdateRequest): Profile {
        return client.patch("$baseUrl/profiles") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun getProfile(userId: Int): Profile {
        return client.get("$baseUrl/profiles/$userId").body()
    }

    override suspend fun uploadMedia(
        file: ByteArray,
        fileName: String,
        thumbnail: ByteArray,
        thumbnailFileName: String
    ): MediaItem {
        return client.submitFormWithBinaryData(
            url = "$baseUrl/media",
            formData = formData {
                append("file", file, Headers.build {
                    append(io.ktor.http.HttpHeaders.ContentType, "multipart/form-data")
                    append(io.ktor.http.HttpHeaders.ContentDisposition, "filename=$fileName")
                })

                append("thumbnail", thumbnail, Headers.build {
                    append(io.ktor.http.HttpHeaders.ContentType, "multipart/form-data")
                    append(io.ktor.http.HttpHeaders.ContentDisposition, "filename=$thumbnailFileName")
                })
            }
        ).body()
    }

    override suspend fun searchProfiles(request: SearchRequest): SearchResponse {
        return client.post("$baseUrl/profiles/search") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }


    // Chat endpoints
    override suspend fun getChats(): List<Chat> {
        return client.get("$baseUrl/chats").body()
    }

    override suspend fun getChat(chatId: String): Chat {
        return client.get("$baseUrl/chats/$chatId").body()
    }

    override suspend fun getChatMessages(chatId: String, limit: Int?, offset: Int?): List<ChatMessage> {
        return client.get("$baseUrl/chats/$chatId/messages") {
            limit?.let { parameter("limit", it) }
            offset?.let { parameter("offset", it) }
        }.body()
    }

    override suspend fun sendMessage(chatId: String, request: SendMessageRequest): ChatMessage {
        return client.post("$baseUrl/chats/$chatId/messages") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun addParticipant(chatId: String, request: AddParticipantRequest): String {
        return client.post("$baseUrl/chats/$chatId/participants") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun removeParticipant(chatId: String, userId: Int): String {
        return client.delete("$baseUrl/chats/$chatId/participants/$userId").body()
    }

    override suspend fun addReaction(messageId: String, request: AddReactionRequest): Map<String, String> {
        return client.post("$baseUrl/messages/$messageId/reactions") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun removeReaction(messageId: String, reactionCode: String): Map<String, String> {
        return client.delete("$baseUrl/messages/$messageId/reactions/$reactionCode").body()
    }

    override suspend fun getOrCreateDirectChat(userID: Int): ChatIDResponse {
        return client.post("$baseUrl/chats/direct") {
            contentType(ContentType.Application.Json)
            setBody(GetOrCreateDirectChatRequest(user_id = userID))
        }.body()
    }

    override suspend fun registerPushToken(request: RegisterPushTokenRequest): Map<String, String> {
        return client.post("$baseUrl/push/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun unregisterPushToken(request: UnregisterPushTokenRequest): Map<String, String> {
        return client.delete("$baseUrl/push/unregister") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}

class BrigadkaApiServiceImpl(
    private val unauthorizedService: BrigadkaApiServiceUnauthorized,
    private val authorizedService: BrigadkaApiServiceAuthorized
) : BrigadkaApiService,
    BrigadkaApiServiceUnauthorized by unauthorizedService,
    BrigadkaApiServiceAuthorized by authorizedService

fun createUnauthorizedKtorClient() = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    // Logging
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.ALL
    }
}

expect fun getHttpClientEngine(): HttpClientEngine

fun createAuthorizedKtorClient(authTokenRepository: AuthTokenRepository, refreshAccessToken: suspend (String) -> Token?) = HttpClient(
    getHttpClientEngine()
) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    install(WebSockets)
    // Logging
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.ALL
    }
    install(Auth) {
        bearer {
            loadTokens {
                authTokenRepository.token.first().let { token ->
                    if (token.accessToken != null && token.refreshToken != null) {
                        BearerTokens(token.accessToken, token.refreshToken)
                    } else {
                        null
                    }
                }
            }
            refreshTokens {
                try {
                    val oldRefreshToken = this.oldTokens?.refreshToken
                    if(oldRefreshToken == null){
                        authTokenRepository.clearToken()
                        return@refreshTokens null
                    }
                    val newToken = refreshAccessToken(oldRefreshToken)
                    if (newToken != null) {
                        authTokenRepository.saveToken(newToken)
                        BearerTokens(newToken.accessToken!!, newToken.refreshToken!!)
                    } else {
                        authTokenRepository.clearToken()
                        null
                    }
                } catch (e: ClientRequestException) {
                    if (e.response.status == HttpStatusCode.Unauthorized) {
                        authTokenRepository.clearToken()
                        null
                    } else {
                        throw e
                    }
                }
            }
        }
    }
}