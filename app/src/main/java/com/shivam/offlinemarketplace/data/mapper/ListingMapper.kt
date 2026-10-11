package com.shivam.offlinemarketplace.data.mapper

import com.shivam.offlinemarketplace.data.local.ListingEntity
import com.shivam.offlinemarketplace.domain.model.Listing
import com.shivam.offlinemarketplace.domain.model.SyncStatus

fun ListingEntity.toDomain(): Listing {
    val status = try {
        SyncStatus.valueOf(syncStatus)
    } catch (_: IllegalArgumentException) {
        SyncStatus.SYNCED
    }
    return Listing(
        id = id,
        remoteId = remoteId,
        title = title,
        description = description,
        price = price,
        category = category,
        localImageUri = localImageUri,
        remoteImageUrl = remoteImageUrl,
        isFavorite = isFavorite,
        syncStatus = status,
        updatedAt = updatedAt
    )
}

fun Listing.toEntity(): ListingEntity {
    return ListingEntity(
        id = id,
        remoteId = remoteId,
        title = title,
        description = description,
        price = price,
        category = category,
        localImageUri = localImageUri,
        remoteImageUrl = remoteImageUrl,
        isFavorite = isFavorite,
        syncStatus = syncStatus.name,
        updatedAt = updatedAt
    )
}
