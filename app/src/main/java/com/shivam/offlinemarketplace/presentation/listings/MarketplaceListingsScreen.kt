package com.shivam.offlinemarketplace.presentation.listings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shivam.offlinemarketplace.MarketplaceApplication
import com.shivam.offlinemarketplace.R
import com.shivam.offlinemarketplace.di.ViewModelFactory
import com.shivam.offlinemarketplace.domain.model.Listing
import com.shivam.offlinemarketplace.domain.model.SyncStatus
import com.shivam.offlinemarketplace.presentation.listings.components.ListingCard

@Composable
fun MarketplaceListingsScreen(
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ListingsViewModel = viewModel(
        factory = ViewModelFactory(MarketplaceApplication.appContainer(LocalContext.current))
    )
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MarketplaceListingsContent(
        uiState = uiState,
        onNavigateToCreate = onNavigateToCreate,
        onNavigateToEdit = onNavigateToEdit,
        onToggleFavorite = { listingId, isFavorite ->
            viewModel.onToggleFavorite(listingId, isFavorite)
            val messageResId = if (!isFavorite) {
                R.string.toast_item_added_to_favorites
            } else {
                R.string.toast_item_removed_from_favorites
            }
            Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show()
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceListingsContent(
    uiState: ListingsUiState,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.marketplace_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.create_new_listing)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading && uiState.listings.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.listings.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.no_listings_empty_state),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.listings,
                            key = { it.id }
                        ) { listing ->
                            ListingCard(
                                listing = listing,
                                onClick = { onNavigateToEdit(listing.id) },
                                onFavoriteToggle = {
                                    onToggleFavorite(listing.id, listing.isFavorite)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Marketplace Listings Screen Preview")
@Composable
fun MarketplaceListingsScreenPreview() {
    MaterialTheme {
        MarketplaceListingsContent(
            uiState = ListingsUiState(
                isLoading = false,
                listings = listOf(
                    Listing(
                        id = "sample_1",
                        title = "Wireless Noise-Canceling Headphones",
                        description = "High quality headphones",
                        price = 199.99,
                        category = "Electronics",
                        isFavorite = true,
                        syncStatus = SyncStatus.SYNCED
                    ),
                    Listing(
                        id = "sample_2",
                        title = "Ergonomic Mesh Chair",
                        description = "Comfortable office chair",
                        price = 149.50,
                        category = "Furniture",
                        isFavorite = false,
                        syncStatus = SyncStatus.PENDING
                    )
                )
            ),
            onNavigateToCreate = {},
            onNavigateToEdit = {},
            onToggleFavorite = { _, _ -> }
        )
    }
}
