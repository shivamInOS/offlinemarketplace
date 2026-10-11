package com.shivam.offlinemarketplace.data.remote

import com.shivam.offlinemarketplace.data.remote.model.CreateListingRequest
import com.shivam.offlinemarketplace.data.remote.model.RemoteListingResponse
import com.shivam.offlinemarketplace.data.remote.model.UpdateListingRequest
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

// Mock remote data source simulating backend REST endpoints with latency and error injection
@Singleton
class FakeMarketplaceRemoteDataSource @Inject constructor() : MarketplaceRemoteDataSource {

    // Configurable error simulator for unit/integration testing
    var simulateException: Throwable? = null
    var simulateDelayMs: Long = 300L

    override suspend fun getListings(): List<RemoteListingResponse> {
        if (simulateDelayMs > 0) {
            delay(simulateDelayMs.milliseconds)
        }
        simulateException?.let { throw it }
        return (1..200).map { i ->
            RemoteListingResponse(
                remoteId = "sample_$i",
                title = "Sample Item $i",
                description = "Description for item $i",
                price = 10.0 + (i * 2),
                category = "Electronics",
                remoteImageUrl = "https://picsum.photos/seed/$i/400/300",
                updatedAt = 1710000000000L + (i * 60_000L)
            )
        }
    }

    override suspend fun createListing(request: CreateListingRequest): RemoteListingResponse {
        if (simulateDelayMs > 0) {
            delay(simulateDelayMs.milliseconds)
        }

        simulateException?.let { throw it }

        if (request.title.contains("[fail]", ignoreCase = true)) {
            throw IllegalArgumentException("Simulated 400 Bad Request: Server content policy rejection")
        }
        if (request.title.contains("[retry]", ignoreCase = true)) {
            throw java.io.IOException("Simulated 503 Network Timeout: Server unreachable")
        }

        val generatedRemoteId = "remote_${request.localId.take(8)}_${UUID.randomUUID().toString().take(6)}"
        val simulatedRemoteImageUrl = if (request.localImageUri != null) {
            "https://marketplace.mock.api/images/$generatedRemoteId.jpg"
        } else {
            null
        }

        return RemoteListingResponse(
            remoteId = generatedRemoteId,
            title = request.title,
            description = request.description,
            price = request.price,
            category = request.category,
            remoteImageUrl = simulatedRemoteImageUrl,
            updatedAt = System.currentTimeMillis()
        )
    }

    override suspend fun updateListing(remoteId: String, request: UpdateListingRequest): RemoteListingResponse {
        if (simulateDelayMs > 0) {
            delay(simulateDelayMs.milliseconds)
        }

        simulateException?.let { throw it }

        if (request.title.contains("[fail]", ignoreCase = true)) {
            throw IllegalArgumentException("Simulated 400 Bad Request: Server content policy rejection")
        }
        if (request.title.contains("[retry]", ignoreCase = true)) {
            throw java.io.IOException("Simulated 503 Network Timeout: Server unreachable")
        }

        return RemoteListingResponse(
            remoteId = remoteId,
            title = request.title,
            description = request.description,
            price = request.price,
            category = request.category,
            remoteImageUrl = null,
            updatedAt = System.currentTimeMillis()
        )
    }
}
