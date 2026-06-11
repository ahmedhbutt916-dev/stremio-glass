package com.stremio.glass.data.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class AuthRequest(
    val email: String,
    val password: String,
    val facebook: Boolean = false,
    val type: String = "email"
)

@Serializable
data class AuthResponse(
    val token: String = "",
    val user: AuthUser = AuthUser()
)

@Serializable
data class AuthUser(
    val _id: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val avatar: String = "",
    val stremioPlus: Boolean = false,
    val premium: Boolean = false,
    val anonymous: Boolean = false
)

class StremioAuthApi {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(json)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 10_000
        }
    }

    companion object {
        private const val API_BASE = "https://api.strem.io"
    }

    suspend fun login(email: String, password: String): AuthResponse {
        return client.post("$API_BASE/api/login") {
            contentType(ContentType.Application.Json)
            setBody(AuthRequest(email, password))
        }.body()
    }

    suspend fun register(email: String, password: String): AuthResponse {
        return client.post("$API_BASE/api/register") {
            contentType(ContentType.Application.Json)
            setBody(AuthRequest(email, password))
        }.body()
    }

    fun close() {
        client.close()
    }
}
