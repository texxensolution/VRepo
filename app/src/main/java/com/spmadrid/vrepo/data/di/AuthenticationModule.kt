package com.spmadrid.vrepo.data.di

import android.content.Context
import com.spmadrid.vrepo.data.services.AuthenticationServiceImpl
import com.spmadrid.vrepo.domain.services.AuthenticationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object AuthenticationModule {
    @Provides
    fun provideAuthenticationService(
        @ApplicationContext context: Context,
    ): AuthenticationService {
        return AuthenticationServiceImpl(
            context
        )
    }
}