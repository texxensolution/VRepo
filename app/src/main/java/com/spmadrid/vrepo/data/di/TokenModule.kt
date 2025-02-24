package com.spmadrid.vrepo.data.di

import android.content.Context
import com.spmadrid.vrepo.domain.services.TokenManagerService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // This makes it available globally
object TokenModule {
    @Provides
    @Singleton
    fun provideTokenManagerService(@ApplicationContext context: Context): TokenManagerService {
        return TokenManagerService(context)
    }
}