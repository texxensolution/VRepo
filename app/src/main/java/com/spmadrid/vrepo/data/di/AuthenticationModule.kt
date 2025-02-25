package com.spmadrid.vrepo.data.di

import android.app.Activity
import android.content.Context
import android.media.session.MediaSession.Token
import com.spmadrid.vrepo.data.repositories.AuthenticationRepositoryImpl
import com.spmadrid.vrepo.data.services.AuthenticationServiceImpl
import com.spmadrid.vrepo.domain.repositories.AuthenticationRepository
import com.spmadrid.vrepo.domain.services.AuthenticationService
import com.spmadrid.vrepo.domain.services.TokenManagerService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AuthenticationModule {
    @Provides
    @Singleton
    fun provideAuthenticationRepository(
        client: HttpClient
    ): AuthenticationRepository {
        return AuthenticationRepositoryImpl(
            client
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
}