package com.shivam.offlinemarketplace.data.remote

import com.shivam.offlinemarketplace.data.remote.model.CreateListingRequest
import com.shivam.offlinemarketplace.data.remote.model.RemoteListingResponse
import com.shivam.offlinemarketplace.data.remote.model.UpdateListingRequest

interface MarketplaceRemoteDataSource {
    suspend fun getListings(): List<RemoteListingResponse>
    suspend fun createListing(request: CreateListingRequest): RemoteListingResponse
    suspend fun updateListing(remoteId: String, request: UpdateListingRequest): RemoteListingResponse
}
