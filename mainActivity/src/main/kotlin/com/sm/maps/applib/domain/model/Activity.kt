package com.sm.maps.applib.domain.model

/**
 * Domain model for Activity
 * Represents an activity type in the business logic layer
 */
data class Activity(
    val id: Int = 0,
    val name: String = ""
) {
    /**
     * Check if this is a new activity (not yet persisted)
     */
    fun isNew(): Boolean = id == 0

    /**
     * Check if activity has a valid name
     */
    fun hasValidName(): Boolean = name.isNotBlank()

    companion object {
        /**
         * Create an empty activity
         */
        fun empty() = Activity()

        /**
         * Create an activity with just a name (new activity)
         */
        fun withName(name: String) = Activity(id = 0, name = name)
    }
}
