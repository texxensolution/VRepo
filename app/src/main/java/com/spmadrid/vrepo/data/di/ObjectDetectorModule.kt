package com.spmadrid.vrepo.data.di

import android.content.Context
import com.spmadrid.vrepo.data.repositories.ObjectDetectorImpl
import com.spmadrid.vrepo.domain.repositories.IObjectDetector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
class ObjectDetectorModule {
    @Provides
    fun provideObjectDetector(@ApplicationContext context: Context): IObjectDetector {
        val modelPath = "model_float16.tflite"
        val labelPath = "labels.txt"

        return ObjectDetectorImpl(
             context,
            labelPath = labelPath,
            modelPath = modelPath
        )
    }
}