package com.shivam.offlinemarketplace.presentation.edit

data class EditListingUiState(
    val listingId: String? = null,
    val isCreateMode: Boolean = true,
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val category: String = "Electronics",
    val imagePath: String? = null,
    val remoteImageUrl: String? = null,
    val isFavorite: Boolean = false,
    val remoteId: String? = null,
    val titleError: String? = null,
    val descriptionError: String? = null,
    val priceError: String? = null,
    val categoryError: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaveCompleted: Boolean = false,
    val generalError: String? = null
)
