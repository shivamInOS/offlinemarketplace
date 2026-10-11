package com.shivam.offlinemarketplace.domain.repository

import com.shivam.offlinemarketplace.domain.model.Listing
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository interface defining core marketplace operations.
 */
interface MarketplaceRepository {
    fun observeListings(): Flow<List<Listing>>
    fun observeListing(id: String): Flow<Listing?>
    suspend fun getListing(id: String): Listing?
    suspend fun seedListingsIfRequired()
    suspend fun saveListing(listing: Listing)
    suspend fun toggleFavorite(id: String, isFavorite: Boolean)
    suspend fun syncPendingListings(): Result<Unit>
}
