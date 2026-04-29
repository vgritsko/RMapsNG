package com.sm.maps.applib.domain.model

/**
 * Domain model for Category
 * Represents a POI category in the business logic layer
 */
data class Category(
    val id: Int = 0,
    val name: String = "",
    val hidden: Boolean = false,
    val iconId: Int? = null,
    val minZoom: Int = 14
) {
    /**
     * Check if this is a new category (not yet persisted)
     */
    fun isNew(): Boolean = id == 0

    /**
     * Check if category is visible (not hidden)
     */
    fun isVisible(): Boolean = !hidden

    companion object {
        /**
         * Create an empty category
         */
        fun empty() = Category()
    }
}
