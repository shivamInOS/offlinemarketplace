package com.shivam.offlinemarketplace.presentation.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.shivam.offlinemarketplace.MarketplaceApplication
import com.shivam.offlinemarketplace.R
import com.shivam.offlinemarketplace.core.util.LocalImageStorage
import com.shivam.offlinemarketplace.di.ViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun EditListingScreen(
    listingId: String?,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditListingViewModel = viewModel(
        factory = ViewModelFactory(
            appContainer = MarketplaceApplication.appContainer(LocalContext.current),
            savedStateHandle = SavedStateHandle(mapOf("listingId" to (listingId ?: "new")))
        )
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val savedPath = LocalImageStorage.saveImageToInternalStorage(context, uri)
                if (savedPath != null) {
                    viewModel.onImageSelected(savedPath)
                }
            }
        }
    }

    LaunchedEffect(uiState.isSaveCompleted) {
        if (uiState.isSaveCompleted) {
            onNavigateUp()
        }
    }

    EditListingContent(
        uiState = uiState,
        onNavigateUp = onNavigateUp,
        onTitleChanged = viewModel::onTitleChanged,
        onDescriptionChanged = viewModel::onDescriptionChanged,
        onPriceChanged = viewModel::onPriceChanged,
        onCategoryChanged = viewModel::onCategoryChanged,
        onPickImage = {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onSave = viewModel::onSave,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditListingContent(
    uiState: EditListingUiState,
    onNavigateUp: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onPriceChanged: (String) -> Unit,
    onCategoryChanged: (String) -> Unit,
    onPickImage: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val availableCategories = listOf("Electronics", "Furniture", "Books", "Fashion", "Other")
    val imageModel = uiState.imagePath ?: uiState.remoteImageUrl

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isCreateMode) {
                            stringResource(R.string.title_create_listing)
                        } else {
                            stringResource(R.string.title_edit_listing)
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_navigate_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Image Preview & Picker
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable(onClick = onPickImage),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageModel != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageModel)
                                .crossfade(true)
                                .build(),
                            contentDescription = stringResource(R.string.cd_selected_listing_image),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = stringResource(R.string.cd_add_image),
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.tap_to_choose_photo),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onPickImage) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (imageModel != null) {
                                stringResource(R.string.change_photo)
                            } else {
                                stringResource(R.string.select_photo)
                            }
                        )
                    }
                }

                // Title Input
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = onTitleChanged,
                    label = { Text(stringResource(R.string.label_title)) },
                    isError = uiState.titleError != null,
                    supportingText = {
                        uiState.titleError?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Category Selector
                Text(
                    text = stringResource(R.string.label_category),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableCategories.forEach { cat ->
                        val isSelected = uiState.category.equals(cat, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onCategoryChanged(cat) },
                            label = { Text(cat) },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null
                        )
                    }
                }
                uiState.categoryError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Price Input
                OutlinedTextField(
                    value = uiState.price,
                    onValueChange = onPriceChanged,
                    label = { Text(stringResource(R.string.label_price)) },
                    isError = uiState.priceError != null,
                    supportingText = {
                        uiState.priceError?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Description Input
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = onDescriptionChanged,
                    label = { Text(stringResource(R.string.label_description)) },
                    isError = uiState.descriptionError != null,
                    supportingText = {
                        uiState.descriptionError?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                // General Error
                uiState.generalError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Save Button
                Button(
                    onClick = onSave,
                    enabled = !uiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (uiState.isCreateMode) {
                                stringResource(R.string.btn_save_listing_offline)
                            } else {
                                stringResource(R.string.btn_update_listing)
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Create Listing Preview")
@Composable
fun EditListingScreenCreatePreview() {
    MaterialTheme {
        EditListingContent(
            uiState = EditListingUiState(
                isCreateMode = true,
                title = "Mechanical Keyboard",
                description = "Tactile mechanical keyboard with RGB lighting.",
                price = "89.99",
                category = "Electronics"
            ),
            onNavigateUp = {},
            onTitleChanged = {},
            onDescriptionChanged = {},
            onPriceChanged = {},
            onCategoryChanged = {},
            onPickImage = {},
            onSave = {}
        )
    }
}

@Preview(showBackground = true, name = "Edit Listing Preview with Error")
@Composable
fun EditListingScreenEditPreview() {
    MaterialTheme {
        EditListingContent(
            uiState = EditListingUiState(
                isCreateMode = false,
                listingId = "sample_1",
                title = "",
                titleError = "Title must be at least 3 characters",
                description = "Ergonomic chair",
                price = "-10",
                priceError = "Price must be greater than 0",
                category = "Furniture"
            ),
            onNavigateUp = {},
            onTitleChanged = {},
            onDescriptionChanged = {},
            onPriceChanged = {},
            onCategoryChanged = {},
            onPickImage = {},
            onSave = {}
        )
    }
}
