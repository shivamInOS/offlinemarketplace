package com.shivam.offlinemarketplace.data.remote.api

import com.shivam.offlinemarketplace.data.remote.model.CreateListingRequest
import com.shivam.offlinemarketplace.data.remote.model.RemoteListingResponse
import com.shivam.offlinemarketplace.data.remote.model.UpdateListingRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface MarketplaceApiService {

    @GET("api/v1/listings")
    suspend fun getListings(): List<RemoteListingResponse>

    @POST("api/v1/listings")
    suspend fun createListing(
        @Body request: CreateListingRequest
    ): RemoteListingResponse

    @PUT("api/v1/listings/{remoteId}")
    suspend fun updateListing(
        @Path("remoteId") remoteId: String,
        @Body request: UpdateListingRequest
    ): RemoteListingResponse
}
