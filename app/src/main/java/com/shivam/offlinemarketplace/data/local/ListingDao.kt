package com.shivam.offlinemarketplace.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ListingDao {

    @Query("SELECT * FROM listings ORDER BY updatedAt DESC")
    fun observeListings(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE id = :id")
    fun observeListing(id: String): Flow<ListingEntity?>

    @Query("SELECT * FROM listings WHERE id = :id LIMIT 1")
    suspend fun getListing(id: String): ListingEntity?

    @Query("SELECT * FROM listings WHERE syncStatus IN ('PENDING', 'FAILED') ORDER BY updatedAt ASC")
    suspend fun getListingsToSync(): List<ListingEntity>

    @Query("SELECT COUNT(*) FROM listings")
    suspend fun getCount(): Int

    @Upsert
    suspend fun upsertListing(listing: ListingEntity): Long

    @Upsert
    suspend fun upsertListings(listings: List<ListingEntity>): List<Long>

    @Query("UPDATE listings SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean): Int

    @Query("""
        UPDATE listings 
        SET remoteId = :remoteId, 
            remoteImageUrl = :remoteImageUrl, 
            syncStatus = :syncStatus, 
            updatedAt = :updatedAt 
        WHERE id = :id
    """)
    suspend fun updateSyncResult(
        id: String,
        remoteId: String?,
        remoteImageUrl: String?,
        syncStatus: String,
        updatedAt: Long
    ): Int
}
