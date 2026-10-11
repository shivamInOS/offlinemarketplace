package com.shivam.offlinemarketplace.di

import android.content.Context
import androidx.room.Room
import com.shivam.offlinemarketplace.data.local.ListingDao
import com.shivam.offlinemarketplace.data.local.MarketplaceDatabase
import com.shivam.offlinemarketplace.data.remote.FakeMarketplaceRemoteDataSource
import com.shivam.offlinemarketplace.data.remote.MarketplaceRemoteDataSource
import com.shivam.offlinemarketplace.data.remote.RetrofitMarketplaceRemoteDataSource
import com.shivam.offlinemarketplace.data.remote.api.MarketplaceApiService
import com.shivam.offlinemarketplace.data.remote.interceptor.MockMarketplaceInterceptor
import com.shivam.offlinemarketplace.data.repository.MarketplaceRepositoryImpl
import com.shivam.offlinemarketplace.data.sync.SyncScheduler
import com.shivam.offlinemarketplace.data.sync.WorkManagerSyncScheduler
import com.shivam.offlinemarketplace.domain.repository.MarketplaceRepository
import com.shivam.offlinemarketplace.domain.usecase.GetListingUseCase
import com.shivam.offlinemarketplace.domain.usecase.ObserveListingsUseCase
import com.shivam.offlinemarketplace.domain.usecase.SaveListingUseCase
import com.shivam.offlinemarketplace.domain.usecase.ToggleFavoriteUseCase
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// App dependency container providing singleton instances across the app
interface AppContainer {
    val marketplaceRepository: MarketplaceRepository
    val observeListingsUseCase: ObserveListingsUseCase
    val getListingUseCase: GetListingUseCase
    val saveListingUseCase: SaveListingUseCase
    val toggleFavoriteUseCase: ToggleFavoriteUseCase
    val syncScheduler: SyncScheduler
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    val database: MarketplaceDatabase by lazy {
        Room.databaseBuilder(
            context,
            MarketplaceDatabase::class.java,
            "marketplace.db"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
    }

    val listingDao: ListingDao by lazy {
        database.listingDao()
    }

    val mockInterceptor: MockMarketplaceInterceptor by lazy {
        MockMarketplaceInterceptor()
    }

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(mockInterceptor)
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.marketplace.mock/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: MarketplaceApiService by lazy {
        retrofit.create(MarketplaceApiService::class.java)
    }

    val remoteDataSource: MarketplaceRemoteDataSource by lazy {
        RetrofitMarketplaceRemoteDataSource(apiService)
    }

    override val syncScheduler: SyncScheduler by lazy {
        WorkManagerSyncScheduler(context)
    }

    override val marketplaceRepository: MarketplaceRepository by lazy {
        MarketplaceRepositoryImpl(
            listingDao = listingDao,
            remoteDataSource = remoteDataSource,
            syncScheduler = syncScheduler
        )
    }

    override val observeListingsUseCase: ObserveListingsUseCase by lazy {
        ObserveListingsUseCase(marketplaceRepository)
    }

    override val getListingUseCase: GetListingUseCase by lazy {
        GetListingUseCase(marketplaceRepository)
    }

    override val saveListingUseCase: SaveListingUseCase by lazy {
        SaveListingUseCase(marketplaceRepository)
    }

    override val toggleFavoriteUseCase: ToggleFavoriteUseCase by lazy {
        ToggleFavoriteUseCase(marketplaceRepository)
    }
}
