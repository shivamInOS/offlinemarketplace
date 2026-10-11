package com.shivam.offlinemarketplace.domain.model

data class Listing(
    val id: String,
    val remoteId: String? = null,
    val title: String,
    val description: String,
    val price: Double,
    val category: String,
    val localImageUri: String? = null,
    val remoteImageUrl: String? = null,
    val isFavorite: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val updatedAt: Long = System.currentTimeMillis()
)
