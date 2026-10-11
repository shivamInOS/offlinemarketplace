package com.shivam.offlinemarketplace

import com.shivam.offlinemarketplace.domain.validation.ListingValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ListingValidatorTest {

    private lateinit var validator: ListingValidator

    @Before
    fun setUp() {
        validator = ListingValidator()
    }

    @Test
    fun `validate with valid inputs returns valid result`() {
        val result = validator.validate(
            title = "Mechanical Keyboard",
            description = "High quality RGB mechanical keyboard.",
            priceString = "99.99",
            category = "Electronics"
        )

        assertTrue(result.isValid)
        assertNull(result.titleError)
        assertNull(result.priceError)
    }

    @Test
    fun `validate with invalid fields returns appropriate errors`() {
        val invalidTitle = validator.validate(
            title = "AB", // Less than 3 chars
            description = "Valid description",
            priceString = "10.0",
            category = "Books"
        )
        assertFalse(invalidTitle.isValid)
        assertEquals("Title must be at least 3 characters", invalidTitle.titleError)

        val invalidPrice = validator.validate(
            title = "Valid Title",
            description = "Valid description",
            priceString = "-5.0", // Negative price
            category = "Books"
        )
        assertFalse(invalidPrice.isValid)
        assertEquals("Price must be greater than 0", invalidPrice.priceError)
    }
}
