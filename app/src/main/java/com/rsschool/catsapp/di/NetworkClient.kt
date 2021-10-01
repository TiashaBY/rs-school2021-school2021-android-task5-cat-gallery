package com.rsschool.catsapp.di

import com.rsschool.catsapp.api.CatsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkClient {

    @Provides
    @Singleton
    fun provideRetrofit() : Retrofit =
        Retrofit.Builder().baseUrl(CatsApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideCatsApi(retrofit: Retrofit) : CatsApi =
        retrofit.create(CatsApi::class.java)
}