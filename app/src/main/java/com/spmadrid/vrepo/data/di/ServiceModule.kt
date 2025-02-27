package com.spmadrid.vrepo.data.di

import android.content.Context
import com.spmadrid.vrepo.data.services.AuthenticationServiceImpl
import com.spmadrid.vrepo.data.services.ServerInfoServiceImpl
import com.spmadrid.vrepo.domain.repositories.AuthenticationRepository
import com.spmadrid.vrepo.domain.repositories.LicensePlateRepository
import com.spmadrid.vrepo.domain.repositories.LocationRepository
import com.spmadrid.vrepo.domain.repositories.ServerInfoRepository
import com.spmadrid.vrepo.domain.services.AuthenticationService
import com.spmadrid.vrepo.domain.services.LicensePlateMatchingService
import com.spmadrid.vrepo.domain.services.LocationManagerService
import com.spmadrid.vrepo.domain.services.ServerInfoService
import com.spmadrid.vrepo.domain.services.TokenManagerService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    @Provides
    @Singleton
    fun provideLocationManagerService(locationRepository: LocationRepository): LocationManagerService {
        return LocationManagerService(locationRepository)
    }

    @Provides
    @Singleton
    fun provideLicensePlateMatchingService(
        licensePlateRepository: LicensePlateRepository
    ): LicensePlateMatchingService {
        return LicensePlateMatchingService (
            licensePlateRepository
        )
    }

    @Provides
    @Singleton
    fun provideAuthenticationService(
        @ApplicationContext context: Context,
        authenticationRepository: AuthenticationRepository
    ): AuthenticationService {
        return AuthenticationServiceImpl(
            authenticationRepository,
            context
        )
    }

    @Provides
    @Singleton
    fun provideTokenManagerService(@ApplicationContext context: Context): TokenManagerService {
        return TokenManagerService(context)
    }

    @Provides
    @Singleton
    fun provideServerInfoService(
        serverInfoRepository: ServerInfoRepository
    ): ServerInfoService {
        return ServerInfoServiceImpl(serverInfoRepository)
    }
}