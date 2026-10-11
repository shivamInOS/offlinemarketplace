package com.shivam.offlinemarketplace.data.repository

import com.shivam.offlinemarketplace.data.local.ListingDao
import com.shivam.offlinemarketplace.data.local.ListingEntity
import com.shivam.offlinemarketplace.data.mapper.toDomain
import com.shivam.offlinemarketplace.data.mapper.toEntity
import com.shivam.offlinemarketplace.data.remote.MarketplaceRemoteDataSource
import com.shivam.offlinemarketplace.data.remote.model.CreateListingRequest
import com.shivam.offlinemarketplace.data.remote.model.UpdateListingRequest
import com.shivam.offlinemarketplace.data.sync.SyncScheduler
import com.shivam.offlinemarketplace.domain.model.Listing
import com.shivam.offlinemarketplace.domain.model.SyncStatus
import com.shivam.offlinemarketplace.domain.repository.MarketplaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketplaceRepositoryImpl @Inject constructor(
    private val listingDao: ListingDao,
    private val remoteDataSource: MarketplaceRemoteDataSource,
    private val syncScheduler: SyncScheduler
) : MarketplaceRepository {

    override fun observeListings(): Flow<List<Listing>> {
        return listingDao.observeListings().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeListing(id: String): Flow<Listing?> {
        return listingDao.observeListing(id).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun getListing(id: String): Listing? {
        return listingDao.getListing(id)?.toDomain()
    }

    override suspend fun seedListingsIfRequired() {
        if (listingDao.getCount() > 0) {
            return
        }

        try {
            val remoteListings = remoteDataSource.getListings()
            val entities = remoteListings.map { remote ->
                ListingEntity(
                    id = remote.remoteId,
                    remoteId = remote.remoteId,
                    title = remote.title,
                    description = remote.description,
                    price = remote.price,
                    category = remote.category,
                    localImageUri = null,
                    remoteImageUrl = remote.remoteImageUrl,
                    isFavorite = false,
                    syncStatus = SyncStatus.SYNCED.name,
                    updatedAt = remote.updatedAt
                )
            }
            listingDao.upsertListings(entities)
        } catch (_: Exception) {
            // Keep database resilient if network fetch fails
        }
    }

    override suspend fun saveListing(listing: Listing) {
        val pendingListing = listing.copy(
            syncStatus = SyncStatus.PENDING,
            updatedAt = System.currentTimeMillis()
        )
        listingDao.upsertListing(pendingListing.toEntity())
        syncScheduler.scheduleSync()
    }

    override suspend fun toggleFavorite(id: String, isFavorite: Boolean) {
        listingDao.updateFavorite(id = id, isFavorite = isFavorite)
    }

    // Syncs pending records with remote backend using Last-Write-Wins (LWW) conflict strategy
    override suspend fun syncPendingListings(): Result<Unit> {
        val pendingEntities = listingDao.getListingsToSync()
        if (pendingEntities.isEmpty()) {
            return Result.success(Unit)
        }

        var firstRetryableException: Throwable? = null

        pendingEntities.forEach { entity ->
            try {
                if (entity.remoteId == null) {
                    // Create remote listing
                    val response = remoteDataSource.createListing(
                        CreateListingRequest(
                            localId = entity.id,
                            title = entity.title,
                            description = entity.description,
                            price = entity.price,
                            category = entity.category,
                            localImageUri = entity.localImageUri
                        )
                    )

                    listingDao.updateSyncResult(
                        id = entity.id,
                        remoteId = response.remoteId,
                        remoteImageUrl = response.remoteImageUrl ?: entity.remoteImageUrl,
                        syncStatus = SyncStatus.SYNCED.name,
                        updatedAt = System.currentTimeMillis()
                    )
                } else {
                    // Update remote listing
                    val response = remoteDataSource.updateListing(
                        remoteId = entity.remoteId,
                        request = UpdateListingRequest(
                            remoteId = entity.remoteId,
                            title = entity.title,
                            description = entity.description,
                            price = entity.price,
                            category = entity.category
                        )
                    )

                    listingDao.updateSyncResult(
                        id = entity.id,
                        remoteId = response.remoteId,
                        remoteImageUrl = response.remoteImageUrl ?: entity.remoteImageUrl,
                        syncStatus = SyncStatus.SYNCED.name,
                        updatedAt = System.currentTimeMillis()
                    )
                }
            } catch (e: IOException) {
                // Retryable network error: keep PENDING in Room
                if (firstRetryableException == null) {
                    firstRetryableException = e
                }
            } catch (_: Exception) {
                // Non-retryable permanent failure: mark FAILED in Room, do not erase local data
                listingDao.updateSyncResult(
                    id = entity.id,
                    remoteId = entity.remoteId,
                    remoteImageUrl = entity.remoteImageUrl,
                    syncStatus = SyncStatus.FAILED.name,
                    updatedAt = System.currentTimeMillis()
                )
            }
        }

        return if (firstRetryableException != null) {
            Result.failure(firstRetryableException)
        } else {
            Result.success(Unit)
        }
    }
}
