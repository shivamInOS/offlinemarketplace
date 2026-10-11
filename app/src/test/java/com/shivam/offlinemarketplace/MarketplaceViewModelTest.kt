package com.shivam.offlinemarketplace

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.shivam.offlinemarketplace.domain.model.Listing
import com.shivam.offlinemarketplace.domain.model.SyncStatus
import com.shivam.offlinemarketplace.domain.usecase.GetListingUseCase
import com.shivam.offlinemarketplace.domain.usecase.ObserveListingsUseCase
import com.shivam.offlinemarketplace.domain.usecase.SaveListingUseCase
import com.shivam.offlinemarketplace.domain.usecase.ToggleFavoriteUseCase
import com.shivam.offlinemarketplace.presentation.edit.EditListingViewModel
import com.shivam.offlinemarketplace.presentation.listings.ListingsViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MarketplaceViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `listingsViewModel observes Room listings and updates uiState`() = runTest {
        val observeListingsUseCase: ObserveListingsUseCase = mockk()
        val toggleFavoriteUseCase: ToggleFavoriteUseCase = mockk(relaxed = true)

        val sampleListings = listOf(
            Listing(
                id = "sample_1",
                title = "Mechanical Keyboard",
                description = "RGB Mechanical",
                price = 99.0,
                category = "Electronics",
                syncStatus = SyncStatus.SYNCED
            )
        )
        every { observeListingsUseCase() } returns flowOf(sampleListings)

        val viewModel = ListingsViewModel(observeListingsUseCase, toggleFavoriteUseCase)

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val state = expectMostRecentItem()
            assertFalse(state.isLoading)
            assertEquals(1, state.listings.size)
            assertEquals("Mechanical Keyboard", state.listings.first().title)
        }
    }

    @Test
    fun `listingsViewModel toggles favorite state via use case`() = runTest {
        val observeListingsUseCase: ObserveListingsUseCase = mockk()
        val toggleFavoriteUseCase: ToggleFavoriteUseCase = mockk(relaxed = true)
        every { observeListingsUseCase() } returns flowOf(emptyList())

        val viewModel = ListingsViewModel(observeListingsUseCase, toggleFavoriteUseCase)

        viewModel.onToggleFavorite("sample_1", false)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { toggleFavoriteUseCase("sample_1", true) }
    }

    @Test
    fun `editListingViewModel saves valid listing with PENDING status and signals completion`() = runTest {
        val getListingUseCase: GetListingUseCase = mockk()
        val saveListingUseCase: SaveListingUseCase = mockk(relaxed = true)
        val savedStateHandle = SavedStateHandle(mapOf("listingId" to "new"))

        val viewModel = EditListingViewModel(savedStateHandle, getListingUseCase, saveListingUseCase)

        viewModel.onTitleChanged("Gaming Mouse")
        viewModel.onDescriptionChanged("Ergonomic wireless mouse")
        viewModel.onPriceChanged("49.99")
        viewModel.onCategoryChanged("Electronics")

        val slot = slot<Listing>()
        coEvery { saveListingUseCase(capture(slot)) } returns Unit

        viewModel.onSave()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertTrue(state.isSaveCompleted)
        }

        coVerify(exactly = 1) { saveListingUseCase(any()) }
        assertEquals("Gaming Mouse", slot.captured.title)
        assertEquals(SyncStatus.PENDING, slot.captured.syncStatus)
    }
}
