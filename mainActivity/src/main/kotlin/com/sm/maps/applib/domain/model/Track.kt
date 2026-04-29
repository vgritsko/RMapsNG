package com.sm.maps.applib.domain.model

data class Track(
    val id: Int = 0,
    val name: String = "",
    val description: String = "",
    val date: Long = 0L,
    val visible: Boolean = false,
    val pointCount: Int = 0,
    val duration: Int = 0,
    val distance: Int = 0,
    val categoryId: Int = 0,
    val activityId: Int = 0,
    val style: String? = null,
    val points: List<TrackPoint> = emptyList()
) {
    fun isNew(): Boolean = id == 0
    fun isVisible(): Boolean = visible

    companion object {
        fun empty() = Track()
    }
}
