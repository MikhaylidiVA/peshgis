package com.walkroute.app.domain.model

data class CurrentLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 0f,
    val altitude: Double? = null,
    val speed: Float? = null,
    val bearing: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun isEmpty(): Boolean = latitude == 0.0 && longitude == 0.0
    
    companion object {
        val EMPTY = CurrentLocation(0.0, 0.0)
    }
}
