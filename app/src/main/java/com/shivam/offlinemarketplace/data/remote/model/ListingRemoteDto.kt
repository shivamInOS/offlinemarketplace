package com.shivam.offlinemarketplace.data.remote.model

data class CreateListingRequest(
    val localId: String,
    val title: String,
    val description: String,
    val price: Double,
    val category: String,
    val localImageUri: String? = null
)

data class UpdateListingRequest(
    val remoteId: String,
    val title: String,
    val description: String,
    val price: Double,
    val category: String
)

data class RemoteListingResponse(
    val remoteId: String,
    val title: String,
    val description: String,
    val price: Double,
    val category: String,
    val remoteImageUrl: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
