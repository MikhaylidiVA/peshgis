package com.walkroute.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val stepLengthCm: Float = 75f,
    val dailyStepGoal: Int = 10000,
    val preferredUnits: String = "metric"
)
