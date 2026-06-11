package com.stremio.glass.data.repository

import com.stremio.glass.data.api.StremioAddonApi
import com.stremio.glass.data.api.StremioAuthApi
import com.stremio.glass.data.local.*
import com.stremio.glass.data.model.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json

class StremioRepository(
    private val addonApi: StremioAddonApi,
    private val authApi: StremioAuthApi,
    private val database: AppDatabase
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val addonDao = database.addonDao()
    private val libraryDao = database.libraryDao()
    private val searchHistoryDao = database.searchHistoryDao()

    // --- Addon Management ---

    fun getInstalledAddons(): Flow<List<Addon>> = addonDao.getInstalledAddons().map { entities ->
        entities.mapNotNull { entity ->
            try {
                val manifest = json.decodeFromString<Manifest>(entity.addonJson)
                Addon(
                    manifestUrl = entity.manifestUrl,
                    manifest = manifest,
                    installed = entity.installed,
                    enabled = entity.enabled,
                    order = entity.order,
                    lastUpdated = entity.lastUpdated
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun installAddon(manifestUrl: String): Result<Addon> = try {
        val manifest = addonApi.getManifest(manifestUrl)
        val addonEntity = AddonEntity(
            manifestUrl = manifestUrl,
            addonJson = json.encodeToString(Manifest.serializer(), manifest),
            installed = true,
            enabled = true,
            order = System.currentTimeMillis().toInt(),
            lastUpdated = System.currentTimeMillis()
        )
        addonDao.insertAddon(addonEntity)
        Result.success(Addon(manifestUrl = manifestUrl, manifest = manifest, installed = true, enabled = true))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun uninstallAddon(manifestUrl: String) {
        addonDao.deleteAddonByUrl(manifestUrl)
    }

    suspend fun toggleAddon(manifestUrl: String, enabled: Boolean) {
        addonDao.setAddonEnabled(manifestUrl, enabled)
    }

    // --- Catalog ---

    suspend fun getCatalog(
        addonUrl: String,
        type: String,
        id: String,
        extra: Map<String, String> = emptyMap()
    ): Result<List<MetaItem>> = try {
        val result = addonApi.getCatalog(addonUrl, type, id, extra)
        Result.success(result.metas)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // --- Meta ---

    suspend fun getMeta(addonUrl: String, type: String, id: String): Result<MetaItem> = try {
        val result = addonApi.getMeta(addonUrl, type, id)
        Result.success(result.meta)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // --- Streams ---

    suspend fun getStreams(type: String, id: String): Result<List<Stream>> = try {
        val installedAddons = addonDao.getInstalledAddons().filter { it.enabled }
        val streamLists = installedAddons.mapNotNull { entity ->
            try {
                val result = addonApi.getStreams(entity.manifestUrl, type, id)
                result.streams
            } catch (e: Exception) {
                null
            }
        }
        Result.success(streamLists.flatten())
    } catch (e: Exception) {
        Result.failure(e)
    }

    // --- Search ---

    suspend fun search(query: String): Result<List<MetaItem>> = try {
        val installedAddons = addonDao.getInstalledAddons().filter { it.enabled }
        val searchResults = mutableListOf<MetaItem>()
        val seenIds = mutableSetOf<String>()

        for (entity in installedAddons) {
            try {
                val manifest = json.decodeFromString<Manifest>(entity.addonJson)
                for (catalog in manifest.catalogs) {
                    if (catalog.extra.any { it.name == "search" }) {
                        val result = addonApi.getCatalog(
                            entity.manifestUrl,
                            catalog.type,
                            catalog.id,
                            mapOf("search" to query)
                        )
                        for (meta in result.metas) {
                            if (meta.id !in seenIds) {
                                seenIds.add(meta.id)
                                searchResults.add(meta)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Skip failing addons
            }
        }
        // Save to search history
        searchHistoryDao.insertSearch(SearchHistoryEntity(query = query))
        Result.success(searchResults)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // --- Library ---

    fun getLibrary(): Flow<List<LibraryItemEntity>> = libraryDao.getLibrary()

    suspend fun addToLibrary(meta: MetaItem) {
        libraryDao.insertLibraryItem(
            LibraryItemEntity(
                id = meta.id,
                type = meta.type,
                name = meta.name,
                poster = meta.poster,
                background = meta.background,
                description = meta.description,
                releaseInfo = meta.releaseInfo,
                rating = meta.rating,
                year = meta.year,
                genre = meta.genre.joinToString(", ")
            )
        )
    }

    suspend fun removeFromLibrary(id: String) {
        libraryDao.deleteLibraryItemById(id)
    }

    suspend fun isInLibrary(id: String): Boolean = libraryDao.getLibraryItem(id) != null

    suspend fun updateWatchProgress(id: String, progress: Float, videoId: String, season: Int?, episode: Int?) {
        libraryDao.updateWatchProgress(id, System.currentTimeMillis(), progress, videoId, season, episode)
    }

    // --- Search History ---

    fun getSearchHistory(): Flow<List<SearchHistoryEntity>> = searchHistoryDao.getRecentSearches()

    suspend fun clearSearchHistory() = searchHistoryDao.clearAll()

    // --- Auth ---

    suspend fun login(email: String, password: String) = authApi.login(email, password)

    suspend fun register(email: String, password: String) = authApi.register(email, password)

    // --- Default Addons ---

    suspend fun installDefaultAddons() {
        val defaults = listOf(
            "https://v3-cinemeta.strem.io/manifest.json",
            "https://watchhub.strem.io/manifest.json",
            "https://opensubtitles.strem.io/manifest.json",
            "https://stremio-torrentio.strem.fun/manifest.json",
            "https://stremio-addon.debridlink.com/manifest.json",
            "https://addon.embedrise.com/manifest.json"
        )
        for (url in defaults) {
            try {
                val existing = addonDao.getAddonByUrl(url)
                if (existing == null) {
                    installAddon(url)
                }
            } catch (e: Exception) {
                // Skip failing addons
            }
        }
    }
}
