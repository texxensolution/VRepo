package com.spmadrid.vrepo.data.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.spmadrid.vrepo.data.providers.KtorClientProvider
import com.spmadrid.vrepo.data.repositories.AuthenticationRepositoryImpl
import com.spmadrid.vrepo.data.repositories.LicensePlateRepositoryImpl
import com.spmadrid.vrepo.data.repositories.LocationRepositoryImpl
import com.spmadrid.vrepo.data.repositories.ServerInfoRepositoryImpl
import com.spmadrid.vrepo.domain.repositories.AuthenticationRepository
import com.spmadrid.vrepo.domain.repositories.LicensePlateRepository
import com.spmadrid.vrepo.domain.repositories.LocationRepository
import com.spmadrid.vrepo.domain.repositories.ServerInfoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideLicensePlateRepository(ktorClientProvider: KtorClientProvider): LicensePlateRepository {
        return LicensePlateRepositoryImpl(ktorClientProvider)
    }

    @Provides
    @Singleton
    fun provideAuthenticationRepository(
        ktorClientProvider: KtorClientProvider
    ): AuthenticationRepository {
        return AuthenticationRepositoryImpl(
            ktorClientProvider
        )
    }

    @Provides
    @Singleton
    fun provideLocationRepository(
        @ApplicationContext context: Context,
        fusedLocationProviderClient: FusedLocationProviderClient
    ): LocationRepository {
        return LocationRepositoryImpl(
            context,
            fusedLocationProviderClient
        )
    }

    @Provides
    @Singleton
    fun provideServerInfoRepository(
        ktorClientProvider: KtorClientProvider
    ): ServerInfoRepository {
        return ServerInfoRepositoryImpl(ktorClientProvider)
    }
}