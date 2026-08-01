package com.walkroute.app.domain.model

data class UserSettings(
    val id: Int = 0,
    val stepLengthCm: Float = 75f,
    val dailyStepGoal: Int = 10000,
    val preferredUnits: String = "metric" // metric or imperial
)
