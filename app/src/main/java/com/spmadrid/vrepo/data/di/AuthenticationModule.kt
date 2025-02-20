package com.spmadrid.vrepo.data.di

import android.app.Activity
import android.content.Context
import com.spmadrid.vrepo.data.services.AuthenticationServiceImpl
import com.spmadrid.vrepo.domain.services.AuthenticationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
object AuthenticationModule {
    @Provides
    fun provideAuthenticationService(
        @ApplicationContext context: Context,
    ): AuthenticationService {
        return AuthenticationServiceImpl(context)
    }
}