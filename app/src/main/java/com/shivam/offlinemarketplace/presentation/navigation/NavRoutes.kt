package com.shivam.offlinemarketplace.presentation.navigation

object NavRoutes {
    const val LISTINGS = "listings"
    const val EDIT = "edit/{listingId}"
    const val CREATE = "edit/new"

    fun editRoute(listingId: String): String = "edit/$listingId"
}
