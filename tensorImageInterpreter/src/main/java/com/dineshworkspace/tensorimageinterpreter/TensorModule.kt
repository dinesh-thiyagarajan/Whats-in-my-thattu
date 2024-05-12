package com.dineshworkspace.tensorimageinterpreter

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object TensorModule {

    @Provides
    fun providesTensorImageInterpreter(@ApplicationContext context: Context): TensorImageInterpreter =
        TensorImageInterpreter(context)

}