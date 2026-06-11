package com.stremio.glass.data.local

import androidx.room.*
import com.stremio.glass.data.model.Addon
import com.stremio.glass.data.model.Manifest
import kotlinx.serialization.json.Json

@Entity(tableName = "addons")
data class AddonEntity(
    @PrimaryKey val manifestUrl: String,
    val addonJson: String,
    val installed: Boolean = true,
    val enabled: Boolean = true,
    val order: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "library")
data class LibraryItemEntity(
    @PrimaryKey val id: String,
    val type: String = "movie",
    val name: String = "",
    val poster: String = "",
    val background: String = "",
    val description: String = "",
    val releaseInfo: String = "",
    val rating: String = "",
    val year: String = "",
    val genre: String = "",
    val addedAt: Long = System.currentTimeMillis(),
    val lastWatchedAt: Long = 0L,
    val watchProgress: Float = 0f,
    val videoId: String = "",
    val season: Int? = null,
    val episode: Int? = null
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val searchedAt: Long = System.currentTimeMillis()
)

@Dao
interface AddonDao {
    @Query("SELECT * FROM addons WHERE installed = 1 ORDER BY `order` ASC")
    suspend fun getInstalledAddons(): List<AddonEntity>

    @Query("SELECT * FROM addons ORDER BY `order` ASC")
    suspend fun getAllAddons(): List<AddonEntity>

    @Query("SELECT * FROM addons WHERE manifestUrl = :url LIMIT 1")
    suspend fun getAddonByUrl(url: String): AddonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddon(addon: AddonEntity)

    @Delete
    suspend fun deleteAddon(addon: AddonEntity)

    @Query("DELETE FROM addons WHERE manifestUrl = :url")
    suspend fun deleteAddonByUrl(url: String)

    @Query("UPDATE addons SET enabled = :enabled WHERE manifestUrl = :url")
    suspend fun setAddonEnabled(url: String, enabled: Boolean)
}

@Dao
interface LibraryDao {
    @Query("SELECT * FROM library ORDER BY addedAt DESC")
    suspend fun getLibrary(): List<LibraryItemEntity>

    @Query("SELECT * FROM library WHERE id = :id LIMIT 1")
    suspend fun getLibraryItem(id: String): LibraryItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLibraryItem(item: LibraryItemEntity)

    @Delete
    suspend fun deleteLibraryItem(item: LibraryItemEntity)

    @Query("DELETE FROM library WHERE id = :id")
    suspend fun deleteLibraryItemById(id: String)

    @Query("UPDATE library SET lastWatchedAt = :time, watchProgress = :progress, videoId = :videoId, season = :season, episode = :episode WHERE id = :id")
    suspend fun updateWatchProgress(id: String, time: Long, progress: Float, videoId: String, season: Int?, episode: Int?)
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY searchedAt DESC LIMIT 20")
    suspend fun getRecentSearches(): List<SearchHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun deleteSearch(query: String)

    @Query("DELETE FROM search_history")
    suspend fun clearAll()
}

@Database(
    entities = [AddonEntity::class, LibraryItemEntity::class, SearchHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun addonDao(): AddonDao
    abstract fun libraryDao(): LibraryDao
    abstract fun searchHistoryDao(): SearchHistoryDao
}
