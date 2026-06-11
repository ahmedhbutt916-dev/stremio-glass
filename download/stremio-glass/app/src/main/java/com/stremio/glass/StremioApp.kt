package com.stremio.glass

import android.app.Application
import androidx.room.Room
import com.stremio.glass.data.api.StremioAddonApi
import com.stremio.glass.data.api.StremioAuthApi
import com.stremio.glass.data.local.AppDatabase
import com.stremio.glass.data.repository.StremioRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@HiltAndroidApp
class StremioApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Install default addons on first launch
        instance = this
    }

    companion object {
        lateinit var instance: StremioApp
            private set
    }
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "stremio-glass-db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideAddonApi(): StremioAddonApi = StremioAddonApi()

    @Provides
    @Singleton
    fun provideAuthApi(): StremioAuthApi = StremioAuthApi()

    @Provides
    @Singleton
    fun provideRepository(
        addonApi: StremioAddonApi,
        authApi: StremioAuthApi,
        database: AppDatabase
    ): StremioRepository = StremioRepository(addonApi, authApi, database)
}
