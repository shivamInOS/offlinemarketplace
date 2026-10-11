package com.shivam.offlinemarketplace.domain.usecase

import com.shivam.offlinemarketplace.domain.model.Listing
import com.shivam.offlinemarketplace.domain.repository.MarketplaceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveListingsUseCase @Inject constructor(
    private val repository: MarketplaceRepository
) {
    operator fun invoke(): Flow<List<Listing>> = repository.observeListings()
}
