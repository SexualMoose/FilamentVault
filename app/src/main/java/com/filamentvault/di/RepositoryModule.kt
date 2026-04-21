package com.filamentvault.di

import com.filamentvault.data.repository.FilamentRepository
import com.filamentvault.data.repository.FilamentRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFilamentRepository(
        impl: FilamentRepositoryImpl
    ): FilamentRepository
}
