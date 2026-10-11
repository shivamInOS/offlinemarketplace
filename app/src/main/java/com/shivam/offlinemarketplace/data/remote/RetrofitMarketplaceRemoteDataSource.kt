package com.shivam.offlinemarketplace.data.remote

import com.shivam.offlinemarketplace.data.remote.api.MarketplaceApiService
import com.shivam.offlinemarketplace.data.remote.model.CreateListingRequest
import com.shivam.offlinemarketplace.data.remote.model.RemoteListingResponse
import com.shivam.offlinemarketplace.data.remote.model.UpdateListingRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetrofitMarketplaceRemoteDataSource @Inject constructor(
    private val apiService: MarketplaceApiService
) : MarketplaceRemoteDataSource {

    override suspend fun getListings(): List<RemoteListingResponse> {
        return apiService.getListings()
    }

    override suspend fun createListing(request: CreateListingRequest): RemoteListingResponse {
        return apiService.createListing(request)
    }

    override suspend fun updateListing(remoteId: String, request: UpdateListingRequest): RemoteListingResponse {
        return apiService.updateListing(remoteId, request)
    }
}
