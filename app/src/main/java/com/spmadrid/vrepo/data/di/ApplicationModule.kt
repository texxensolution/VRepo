package com.spmadrid.vrepo.data.di

import android.app.Activity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import javax.inject.Singleton


@Module
@InstallIn(ActivityComponent::class)
object ApplicationModule {
//    @Provides
//    fun provideActivity(activity: Activity): Activity = activity
}