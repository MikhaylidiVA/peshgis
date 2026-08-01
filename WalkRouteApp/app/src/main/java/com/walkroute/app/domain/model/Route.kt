package com.walkroute.app.domain.model

data class Route(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val points: List<RoutePoint>,
    val totalDistanceMeters: Int,
    val estimatedSteps: Int,
    val estimatedDurationMinutes: Int,
    val difficulty: Difficulty = Difficulty.EASY,
    val startPoint: RoutePoint,
    val endPoint: RoutePoint,
    val createdAt: Long = System.currentTimeMillis()
) {
    enum class Difficulty {
        EASY, MEDIUM, HARD
    }
}
