package com.walkroute.app.domain.model

data class RoutePoint(
    val latitude: Double,
    val longitude: Double,
    val name: String? = null,
    val instruction: String? = null
)
