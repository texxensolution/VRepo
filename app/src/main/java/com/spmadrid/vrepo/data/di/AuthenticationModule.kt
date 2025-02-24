package com.spmadrid.vrepo.data.di

import android.app.Activity
import android.content.Context
import android.media.session.MediaSession.Token
import com.spmadrid.vrepo.data.services.AuthenticationServiceImpl
import com.spmadrid.vrepo.domain.services.AuthenticationService
import com.spmadrid.vrepo.domain.services.TokenManagerService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AuthenticationModule {
    @Provides
    @Singleton
    fun provideAuthenticationService(
        @ApplicationContext context: Context,
    ): AuthenticationService {
        return AuthenticationServiceImpl(
            context
        )
    }
}