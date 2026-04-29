package com.sm.maps.applib.domain.model

data class PoiPoint(
    val id: Int = 0,
    val name: String = "",
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0,
    val hidden: Boolean = false,
    val categoryId: Int = 0,
    val pointSourceId: Int = 0,
    val iconId: Int? = null
) {
    fun isNew(): Boolean = id == 0
    fun isVisible(): Boolean = !hidden

    companion object {
        fun empty() = PoiPoint()
    }
}
