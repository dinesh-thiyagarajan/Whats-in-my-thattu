package com.dineshworkspace.tensorimageinterpreter

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object TensorModule {

    @Provides
    fun providesTensorImageInterpreter(): TensorImageInterpreter =
        TensorImageInterpreter()

}