package com.shivam.offlinemarketplace.domain.usecase

import com.shivam.offlinemarketplace.domain.repository.MarketplaceRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: MarketplaceRepository
) {
    suspend operator fun invoke(id: String, isFavorite: Boolean) {
        repository.toggleFavorite(id, isFavorite)
    }
}
