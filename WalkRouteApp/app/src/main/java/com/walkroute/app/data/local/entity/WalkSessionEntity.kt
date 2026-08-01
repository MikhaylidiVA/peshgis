package com.walkroute.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "walk_sessions")
data class WalkSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routeId: Long? = null,
    val routeName: String? = null,
    val startTime: Long,
    val endTime: Long? = null,
    val totalSteps: Int = 0,
    val totalDistanceMeters: Int = 0,
    val durationMinutes: Int = 0,
    val caloriesBurned: Int? = null,
    val avgPace: Float? = null,
    val trackPointsJson: String? = null,
    val isCompleted: Boolean = false
)
