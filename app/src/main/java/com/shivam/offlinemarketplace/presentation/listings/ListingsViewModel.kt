package com.shivam.offlinemarketplace.presentation.listings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shivam.offlinemarketplace.domain.usecase.ObserveListingsUseCase
import com.shivam.offlinemarketplace.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListingsViewModel @Inject constructor(
    observeListingsUseCase: ObserveListingsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    val uiState: StateFlow<ListingsUiState> = observeListingsUseCase()
        .map { listings ->
            ListingsUiState(
                listings = listings,
                isLoading = false,
                errorMessage = null
            )
        }
        .catch { error ->
            emit(
                ListingsUiState(
                    listings = emptyList(),
                    isLoading = false,
                    errorMessage = error.localizedMessage ?: "Failed to load listings"
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ListingsUiState(isLoading = true)
        )

    fun onToggleFavorite(listingId: String, currentFavoriteState: Boolean) {
        viewModelScope.launch {
            toggleFavoriteUseCase(listingId, !currentFavoriteState)
        }
    }
}
