package com.filamentvault.di

import android.content.Context
import androidx.room.Room
import com.filamentvault.data.local.FilamentVaultDatabase
import com.filamentvault.data.local.dao.DefaultOverrideDao
import com.filamentvault.data.local.dao.DefaultSettingsDao
import com.filamentvault.data.local.dao.FilamentDao
import com.filamentvault.util.DefaultsPopulator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        defaultsPopulator: DefaultsPopulator
    ): FilamentVaultDatabase {
        return Room.databaseBuilder(
            context,
            FilamentVaultDatabase::class.java,
            "filamentvault.db"
        )
            .addCallback(defaultsPopulator)
            .addMigrations(
                FilamentVaultDatabase.MIGRATION_3_4,
                FilamentVaultDatabase.MIGRATION_4_5
            )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideFilamentDao(database: FilamentVaultDatabase): FilamentDao =
        database.filamentDao()

    @Provides
    fun provideDefaultSettingsDao(database: FilamentVaultDatabase): DefaultSettingsDao =
        database.defaultSettingsDao()

    @Provides
    fun provideDefaultOverrideDao(database: FilamentVaultDatabase): DefaultOverrideDao =
        database.defaultOverrideDao()
}
