package com.stremio.glass.data.api

import android.util.Log
import com.stremio.glass.data.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json

class StremioAddonApi {

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
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 30_000
        }
        followRedirects = true
    }

    suspend fun getManifest(addonUrl: String): Manifest {
        val url = buildUrl(addonUrl, "manifest.json")
        return withRetry("getManifest", addonUrl) {
            client.get(url).body()
        }
    }

    suspend fun getCatalog(
        addonUrl: String,
        type: String,
        id: String,
        extra: Map<String, String> = emptyMap()
    ): CatalogResult {
        val extraPart = if (extra.isNotEmpty()) {
            val encoded = extra.entries.joinToString("&") { (k, v) ->
                "${k.encodeURLParameter()}=${v.encodeURLParameter()}"
            }
            "/$encoded"
        } else ""
        val url = buildUrl(addonUrl, "catalog/$type/$id${extraPart}.json")
        return withRetry("getCatalog", addonUrl) {
            client.get(url).body()
        }
    }

    suspend fun getMeta(addonUrl: String, type: String, id: String): MetaResult {
        val url = buildUrl(addonUrl, "meta/$type/$id.json")
        return withRetry("getMeta", addonUrl) {
            client.get(url).body()
        }
    }

    suspend fun getStreams(addonUrl: String, type: String, id: String): StreamResult {
        val url = buildUrl(addonUrl, "stream/$type/$id.json")
        return withRetry("getStreams", addonUrl) {
            client.get(url).body()
        }
    }

    /**
     * Retry wrapper with exponential backoff for resilient API calls.
     * Retries up to maxRetries times on network/timeout errors.
     */
    private suspend fun <T> withRetry(
        operation: String,
        url: String,
        maxRetries: Int = 2,
        block: suspend () -> T
    ): T {
        var lastException: Exception? = null
        repeat(maxRetries + 1) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxRetries) {
                    val delayMs = 500L * (attempt + 1)
                    Log.d("StremioAddonApi", "$operation failed (attempt ${attempt + 1}/${maxRetries + 1}), retrying in ${delayMs}ms: ${e.message}")
                    delay(delayMs)
                } else {
                    Log.w("StremioAddonApi", "$operation failed after ${maxRetries + 1} attempts for $url: ${e.message}")
                }
            }
        }
        throw lastException!!
    }

    private fun buildUrl(base: String, path: String): String {
        val cleanBase = base.trimEnd('/')
        val cleanPath = path.trimStart('/')
        return "$cleanBase/$cleanPath"
    }

    fun close() {
        client.close()
    }
}
