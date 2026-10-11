package com.shivam.offlinemarketplace.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "listings")
data class ListingEntity(
    @PrimaryKey
    val id: String,
    val remoteId: String?,
    val title: String,
    val description: String,
    val price: Double,
    val category: String,
    val localImageUri: String?,
    val remoteImageUrl: String?,
    val isFavorite: Boolean,
    val syncStatus: String,
    val updatedAt: Long
)
