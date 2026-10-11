package com.shivam.offlinemarketplace.domain.usecase

import com.shivam.offlinemarketplace.domain.model.Listing
import com.shivam.offlinemarketplace.domain.repository.MarketplaceRepository
import javax.inject.Inject

class SaveListingUseCase @Inject constructor(
    private val repository: MarketplaceRepository
) {
    suspend operator fun invoke(listing: Listing) {
        repository.saveListing(listing)
    }
}
