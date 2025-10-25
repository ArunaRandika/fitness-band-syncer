package com.example.fitnessband.di

import android.content.Context
import com.example.fitnessband.data.btdatasource.BleManager
import com.example.fitnessband.data.repository.MiBandRepositoryImpl
import com.example.fitnessband.domain.repository.MiBandRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMiBand6BleManager(
        @ApplicationContext context: Context
    ): BleManager {
        return BleManager(context)
    }

    @Provides
    @Singleton
    fun provideMiBandRepository(
        bleManager: BleManager
    ): MiBandRepository {
        return MiBandRepositoryImpl(bleManager)
    }
}
