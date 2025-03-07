package com.spmadrid.vrepo.data.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.spmadrid.vrepo.data.providers.KtorClientProvider
import com.spmadrid.vrepo.data.repositories.LicensePlateRepositoryImpl
import com.spmadrid.vrepo.data.repositories.LocationRepositoryImpl
import com.spmadrid.vrepo.domain.repositories.LicensePlateRepository
import com.spmadrid.vrepo.domain.repositories.LocationRepository
import com.spmadrid.vrepo.domain.services.LicensePlateMatchingService
import com.spmadrid.vrepo.domain.services.TokenManagerService
import com.spmadrid.vrepo.presentation.viewmodel.AuthenticateViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideKtorClient(
        tokenManagerService: TokenManagerService
    ): HttpClient {
        return HttpClient(CIO) {
            defaultRequest {
                host = "elephant-humble-herring.ngrok-free.app"
                url {
                    protocol = URLProtocol.HTTPS
                }
            }
            install(WebSockets) {
                pingIntervalMillis = 1000L
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        runBlocking {
                            tokenManagerService.tokenFlow.firstOrNull()?.let {
                                BearerTokens(it, "")
                            }
                        }
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
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Provides
    @Singleton
    fun provideKtorProviderClient(tokenManagerService: TokenManagerService): KtorClientProvider {
        return KtorClientProvider(tokenManagerService)
    }
}