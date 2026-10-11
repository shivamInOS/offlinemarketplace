package com.shivam.offlinemarketplace.presentation.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shivam.offlinemarketplace.domain.model.Listing
import com.shivam.offlinemarketplace.domain.model.SyncStatus
import com.shivam.offlinemarketplace.domain.usecase.GetListingUseCase
import com.shivam.offlinemarketplace.domain.usecase.SaveListingUseCase
import com.shivam.offlinemarketplace.domain.validation.ListingValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class EditListingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getListingUseCase: GetListingUseCase,
    private val saveListingUseCase: SaveListingUseCase
) : ViewModel() {

    private val validator = ListingValidator()
    private val listingId: String? = savedStateHandle.get<String>("listingId")
    private val isCreateMode: Boolean = listingId == null || listingId == "new"

    private val _uiState = MutableStateFlow(
        EditListingUiState(
            listingId = listingId,
            isCreateMode = isCreateMode,
            isLoading = !isCreateMode
        )
    )
    val uiState: StateFlow<EditListingUiState> = _uiState.asStateFlow()

    init {
        if (!isCreateMode && listingId != null) {
            loadListing(listingId)
        }
    }

    private fun loadListing(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val listing = getListingUseCase(id)
            if (listing != null) {
                _uiState.update {
                    it.copy(
                        title = listing.title,
                        description = listing.description,
                        price = listing.price.toString(),
                        category = listing.category,
                        imagePath = listing.localImageUri,
                        remoteImageUrl = listing.remoteImageUrl,
                        isFavorite = listing.isFavorite,
                        remoteId = listing.remoteId,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        generalError = "Listing not found"
                    )
                }
            }
        }
    }

    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(title = title, titleError = null) }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.update { it.copy(description = description, descriptionError = null) }
    }

    fun onPriceChanged(price: String) {
        _uiState.update { it.copy(price = price, priceError = null) }
    }

    fun onCategoryChanged(category: String) {
        _uiState.update { it.copy(category = category, categoryError = null) }
    }

    fun onImageSelected(imagePath: String) {
        _uiState.update { it.copy(imagePath = imagePath) }
    }

    fun onSave() {
        val currentState = _uiState.value
        val validationResult = validator.validate(
            title = currentState.title,
            description = currentState.description,
            priceString = currentState.price,
            category = currentState.category
        )

        if (!validationResult.isValid) {
            _uiState.update {
                it.copy(
                    titleError = validationResult.titleError,
                    descriptionError = validationResult.descriptionError,
                    priceError = validationResult.priceError,
                    categoryError = validationResult.categoryError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val listing = Listing(
                id = if (currentState.isCreateMode || currentState.listingId == null) {
                    UUID.randomUUID().toString()
                } else {
                    currentState.listingId
                },
                remoteId = currentState.remoteId,
                title = currentState.title.trim(),
                description = currentState.description.trim(),
                price = currentState.price.trim().toDouble(),
                category = currentState.category.trim(),
                localImageUri = currentState.imagePath,
                remoteImageUrl = currentState.remoteImageUrl,
                isFavorite = currentState.isFavorite,
                syncStatus = SyncStatus.PENDING,
                updatedAt = System.currentTimeMillis()
            )

            saveListingUseCase(listing)

            _uiState.update {
                it.copy(
                    isSaving = false,
                    isSaveCompleted = true
                )
            }
        }
    }
}
