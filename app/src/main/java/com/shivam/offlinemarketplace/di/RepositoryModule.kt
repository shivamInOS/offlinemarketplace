package com.shivam.offlinemarketplace.di

import com.shivam.offlinemarketplace.data.repository.MarketplaceRepositoryImpl
import com.shivam.offlinemarketplace.domain.repository.MarketplaceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMarketplaceRepository(
        impl: MarketplaceRepositoryImpl
    ): MarketplaceRepository
}
