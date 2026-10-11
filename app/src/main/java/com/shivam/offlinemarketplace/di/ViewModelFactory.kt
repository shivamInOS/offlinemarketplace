package com.shivam.offlinemarketplace.di

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shivam.offlinemarketplace.presentation.edit.EditListingViewModel
import com.shivam.offlinemarketplace.presentation.listings.ListingsViewModel

class ViewModelFactory(
    private val appContainer: AppContainer,
    private val savedStateHandle: SavedStateHandle? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ListingsViewModel::class.java) -> {
                ListingsViewModel(
                    observeListingsUseCase = appContainer.observeListingsUseCase,
                    toggleFavoriteUseCase = appContainer.toggleFavoriteUseCase
                ) as T
            }
            modelClass.isAssignableFrom(EditListingViewModel::class.java) -> {
                EditListingViewModel(
                    savedStateHandle = savedStateHandle ?: SavedStateHandle(),
                    getListingUseCase = appContainer.getListingUseCase,
                    saveListingUseCase = appContainer.saveListingUseCase
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
