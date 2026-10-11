package com.shivam.offlinemarketplace.domain.usecase

import com.shivam.offlinemarketplace.domain.model.Listing
import com.shivam.offlinemarketplace.domain.repository.MarketplaceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetListingUseCase @Inject constructor(
    private val repository: MarketplaceRepository
) {
    fun asFlow(id: String): Flow<Listing?> = repository.observeListing(id)
    suspend operator fun invoke(id: String): Listing? = repository.getListing(id)
}
