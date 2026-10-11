package com.shivam.offlinemarketplace

import com.shivam.offlinemarketplace.data.local.ListingDao
import com.shivam.offlinemarketplace.data.local.ListingEntity
import com.shivam.offlinemarketplace.data.remote.MarketplaceRemoteDataSource
import com.shivam.offlinemarketplace.data.remote.model.RemoteListingResponse
import com.shivam.offlinemarketplace.data.repository.MarketplaceRepositoryImpl
import com.shivam.offlinemarketplace.data.sync.SyncScheduler
import com.shivam.offlinemarketplace.domain.model.Listing
import com.shivam.offlinemarketplace.domain.model.SyncStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class MarketplaceRepositoryImplTest {

    private lateinit var listingDao: ListingDao
    private lateinit var remoteDataSource: MarketplaceRemoteDataSource
    private lateinit var syncScheduler: SyncScheduler
    private lateinit var repository: MarketplaceRepositoryImpl

    @Before
    fun setUp() {
        listingDao = mockk(relaxed = true)
        remoteDataSource = mockk()
        syncScheduler = mockk(relaxed = true)
        repository = MarketplaceRepositoryImpl(listingDao, remoteDataSource, syncScheduler)
    }

    @Test
    fun `seedListingsIfRequired inserts 200 items when database is empty and skips when populated`() = runTest {
        // When DB is empty, inserts 200 items fetched from remoteDataSource
        coEvery { listingDao.getCount() } returns 0
        coEvery { remoteDataSource.getListings() } returns (1..200).map { i ->
            RemoteListingResponse(
                remoteId = "sample_$i",
                title = "Item $i",
                description = "Desc $i",
                price = 10.0,
                category = "Electronics"
            )
        }
        val slot = slot<List<ListingEntity>>()
        coEvery { listingDao.upsertListings(capture(slot)) } returns (1L..200L).toList()

        repository.seedListingsIfRequired()

        coVerify(exactly = 1) { remoteDataSource.getListings() }
        coVerify(exactly = 1) { listingDao.upsertListings(any()) }
        assertEquals(200, slot.captured.size)

        // When DB already has items, skips insertion
        coEvery { listingDao.getCount() } returns 200
        repository.seedListingsIfRequired()
        coVerify(exactly = 1) { remoteDataSource.getListings() } // Still 1 (skipped)
        coVerify(exactly = 1) { listingDao.upsertListings(any()) } // Still 1 (skipped)
    }

    @Test
    fun `saveListing writes to Room with PENDING status and schedules sync`() = runTest {
        val slot = slot<ListingEntity>()
        coEvery { listingDao.upsertListing(capture(slot)) } returns 1L

        val listing = Listing(
            id = "test-1",
            title = "Test Item",
            description = "Test Desc",
            price = 49.99,
            category = "Electronics",
            syncStatus = SyncStatus.SYNCED
        )

        repository.saveListing(listing)

        coVerify(exactly = 1) { listingDao.upsertListing(any()) }
        verify(exactly = 1) { syncScheduler.scheduleSync() }
        assertEquals(SyncStatus.PENDING.name, slot.captured.syncStatus)
    }

    @Test
    fun `toggleFavorite updates only local database without scheduling sync`() = runTest {
        coEvery { listingDao.updateFavorite("sample_1", true) } returns 1

        repository.toggleFavorite("sample_1", true)

        coVerify(exactly = 1) { listingDao.updateFavorite("sample_1", true) }
        verify(exactly = 0) { syncScheduler.scheduleSync() } // Zero network sync
    }

    @Test
    fun `syncPendingListings marks entity as SYNCED when remote call succeeds`() = runTest {
        val pendingEntity = ListingEntity(
            id = "local_1",
            remoteId = null,
            title = "New Camera",
            description = "4K Action camera",
            price = 199.99,
            category = "Electronics",
            localImageUri = null,
            remoteImageUrl = null,
            isFavorite = false,
            syncStatus = SyncStatus.PENDING.name,
            updatedAt = 1000L
        )
        coEvery { listingDao.getListingsToSync() } returns listOf(pendingEntity)
        coEvery { remoteDataSource.createListing(any()) } returns RemoteListingResponse(
            remoteId = "remote_123",
            title = "New Camera",
            description = "4K Action camera",
            price = 199.99,
            category = "Electronics",
            remoteImageUrl = "https://cdn.example.com/img.jpg"
        )

        val result = repository.syncPendingListings()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            listingDao.updateSyncResult("local_1", "remote_123", "https://cdn.example.com/img.jpg", SyncStatus.SYNCED.name, any())
        }
    }

    @Test
    fun `syncPendingListings returns failure on IOException to trigger WorkManager retry`() = runTest {
        val pendingEntity = ListingEntity(
            id = "local_1",
            remoteId = null,
            title = "New Camera",
            description = "4K Action camera",
            price = 199.99,
            category = "Electronics",
            localImageUri = null,
            remoteImageUrl = null,
            isFavorite = false,
            syncStatus = SyncStatus.PENDING.name,
            updatedAt = 1000L
        )
        coEvery { listingDao.getListingsToSync() } returns listOf(pendingEntity)
        coEvery { remoteDataSource.createListing(any()) } throws IOException("No internet connection")

        val result = repository.syncPendingListings()

        assertTrue(result.isFailure)
    }

    @Test
    fun `syncPendingListings marks entity as FAILED on non-retryable server error`() = runTest {
        val pendingEntity = ListingEntity(
            id = "local_1",
            remoteId = null,
            title = "Prohibited Item [fail]",
            description = "Test item",
            price = 10.0,
            category = "Other",
            localImageUri = null,
            remoteImageUrl = null,
            isFavorite = false,
            syncStatus = SyncStatus.PENDING.name,
            updatedAt = 1000L
        )
        coEvery { listingDao.getListingsToSync() } returns listOf(pendingEntity)
        coEvery { remoteDataSource.createListing(any()) } throws IllegalArgumentException("400 Bad Request")

        val result = repository.syncPendingListings()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            listingDao.updateSyncResult("local_1", null, null, SyncStatus.FAILED.name, any())
        }
    }
}
