package com.shivam.offlinemarketplace.di

import android.content.Context
import androidx.room.Room
import com.shivam.offlinemarketplace.data.local.ListingDao
import com.shivam.offlinemarketplace.data.local.MarketplaceDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMarketplaceDatabase(
        context: Context
    ): MarketplaceDatabase {
        return Room.databaseBuilder(
            context,
            MarketplaceDatabase::class.java,
            "marketplace.db"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
    }

    @Provides
    @Singleton
    fun provideListingDao(
        database: MarketplaceDatabase
    ): ListingDao {
        return database.listingDao()
    }
}
