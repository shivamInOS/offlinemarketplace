package com.shivam.offlinemarketplace.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ListingEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MarketplaceDatabase : RoomDatabase() {
    abstract fun listingDao(): ListingDao
}
