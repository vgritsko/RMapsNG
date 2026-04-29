package com.sm.maps.applib.domain.model

/**
 * Domain model for Map data
 * Represents a map configuration in the business logic layer
 */
data class MapData(
    val id: Int = 0,
    val name: String = "",
    val type: Int = 0,
    val params: String = ""
) {
    /**
     * Check if this is a new map (not yet persisted)
     */
    fun isNew(): Boolean = id == 0

    /**
     * Check if map has a valid name
     */
    fun hasValidName(): Boolean = name.isNotBlank()

    /**
     * Check if map has parameters configured
     */
    fun hasParams(): Boolean = params.isNotBlank()

    companion object {
        /**
         * Create an empty map
         */
        fun empty() = MapData()

        /**
         * Create a map with basic info (new map)
         */
        fun create(name: String, type: Int, params: String = "") =
            MapData(id = 0, name = name, type = type, params = params)
    }
}
