package com.spmadrid.vrepo.data.di

import com.spmadrid.vrepo.data.repositories.LicensePlateRepositoryImpl
import com.spmadrid.vrepo.domain.repositories.LicensePlateRepository
import com.spmadrid.vrepo.domain.services.LicensePlateMatchingService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideKtorClient(): HttpClient {
        return HttpClient(CIO) {
            defaultRequest {
                host = "elephant-humble-herring.ngrok-free.app"
                url {
                    protocol = URLProtocol.HTTPS
                }
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoib25fZTc0OTdiODQ0YmVlZjZlNDdhOTE1ZjY3MzYxYzhjNjYiLCJ1c2VyX3R5cGUiOiJpbnRlcm5hbCIsImV4cCI6MjA1NTQ3NzE4Nn0.7REIFUOCze2GQdmeoLdkaih1uHzI0nlXPMSHCLgm6Q8"
                        BearerTokens(token, "")
                    }
                }
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    }
                )
            }
        }
    }

    @Provides
    @Singleton
    fun provideLicensePlateRepository(client: HttpClient): LicensePlateRepository {
        return LicensePlateRepositoryImpl(client)
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
}