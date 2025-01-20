package com.himangskalita.nimbus.di.module

import com.himangskalita.nimbus.di.repository.WeatherRepository
import com.himangskalita.nimbus.di.repository.WeatherRepositoryIml
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun getWeatherRepository(weatherRepositoryIml: WeatherRepositoryIml): WeatherRepository

}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun getOkHttpClient() : OkHttpClient {

        return OkHttpClient.Builder().build()
    }
}