package com.walkroute.app.domain.model

data class WalkSession(
    val id: Long = 0,
    val routeId: Long? = null,
    val routeName: String? = null,
    val startTime: Long,
    val endTime: Long? = null,
    val totalSteps: Int = 0,
    val totalDistanceMeters: Int = 0,
    val durationMinutes: Int = 0,
    val caloriesBurned: Int? = null,
    val avgPace: Float? = null, // minutes per km
    val trackPoints: List<TrackPoint> = emptyList(),
    val isCompleted: Boolean = false
) {
    data class TrackPoint(
        val latitude: Double,
        val longitude: Double,
        val timestamp: Long,
        val stepsAtPoint: Int = 0
    )
}
