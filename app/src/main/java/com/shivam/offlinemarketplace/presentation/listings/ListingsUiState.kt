package com.shivam.offlinemarketplace.presentation.listings

import com.shivam.offlinemarketplace.domain.model.Listing

data class ListingsUiState(
    val listings: List<Listing> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
