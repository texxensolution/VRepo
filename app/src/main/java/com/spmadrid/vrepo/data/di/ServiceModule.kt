package com.spmadrid.vrepo.data.di

import com.spmadrid.vrepo.domain.repositories.LocationRepository
import com.spmadrid.vrepo.domain.services.LocationManagerService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    @Provides
    @Singleton
    fun provideLocationManagerService(locationRepository: LocationRepository): LocationManagerService {
        return LocationManagerService(locationRepository)
    }
}