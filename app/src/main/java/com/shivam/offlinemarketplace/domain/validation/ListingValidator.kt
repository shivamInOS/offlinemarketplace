package com.shivam.offlinemarketplace.domain.validation

data class ListingValidationResult(
    val isValid: Boolean,
    val titleError: String? = null,
    val descriptionError: String? = null,
    val priceError: String? = null,
    val categoryError: String? = null
)

class ListingValidator {

    fun validate(
        title: String,
        description: String,
        priceString: String,
        category: String
    ): ListingValidationResult {
        val trimmedTitle = title.trim()
        val trimmedDescription = description.trim()
        val trimmedCategory = category.trim()

        val titleError = when {
            trimmedTitle.isEmpty() -> "Title is required"
            trimmedTitle.length < 3 -> "Title must be at least 3 characters"
            else -> null
        }

        val descriptionError = when {
            trimmedDescription.isEmpty() -> "Description is required"
            else -> null
        }

        val priceDouble = priceString.trim().toDoubleOrNull()
        val priceError = when {
            priceString.trim().isEmpty() -> "Price is required"
            priceDouble == null -> "Price must be a valid number"
            priceDouble <= 0.0 -> "Price must be greater than 0"
            else -> null
        }

        val categoryError = when {
            trimmedCategory.isEmpty() -> "Category is required"
            else -> null
        }

        val isValid = titleError == null &&
                descriptionError == null &&
                priceError == null &&
                categoryError == null

        return ListingValidationResult(
            isValid = isValid,
            titleError = titleError,
            descriptionError = descriptionError,
            priceError = priceError,
            categoryError = categoryError
        )
    }
}
