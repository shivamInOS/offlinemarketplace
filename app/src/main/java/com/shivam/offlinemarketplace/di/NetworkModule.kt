package com.shivam.offlinemarketplace.di

import com.shivam.offlinemarketplace.data.remote.MarketplaceRemoteDataSource
import com.shivam.offlinemarketplace.data.remote.RetrofitMarketplaceRemoteDataSource
import com.shivam.offlinemarketplace.data.remote.api.MarketplaceApiService
import com.shivam.offlinemarketplace.data.remote.interceptor.MockMarketplaceInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMockInterceptor(): MockMarketplaceInterceptor {
        return MockMarketplaceInterceptor()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(mockInterceptor: MockMarketplaceInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(mockInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.marketplace.mock/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideMarketplaceApiService(retrofit: Retrofit): MarketplaceApiService {
        return retrofit.create(MarketplaceApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMarketplaceRemoteDataSource(
        apiService: MarketplaceApiService
    ): MarketplaceRemoteDataSource {
        return RetrofitMarketplaceRemoteDataSource(apiService)
    }
}
