package com.stremio.glass.data.api

import com.stremio.glass.data.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
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
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 10_000
        }
        followRedirects = true
    }

    suspend fun getManifest(addonUrl: String): Manifest {
        val url = buildUrl(addonUrl, "manifest.json")
        return client.get(url).body()
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
        return client.get(url).body()
    }

    suspend fun getMeta(addonUrl: String, type: String, id: String): MetaResult {
        val url = buildUrl(addonUrl, "meta/$type/$id.json")
        return client.get(url).body()
    }

    suspend fun getStreams(addonUrl: String, type: String, id: String): StreamResult {
        val url = buildUrl(addonUrl, "stream/$type/$id.json")
        return client.get(url).body()
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
