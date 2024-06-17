package org.hans.model.trainsasone

import kotlinx.serialization.Serializable

@Serializable
data class TrainsAsOneWorkout(
    val workoutName: String,
    val description: String = "",
    val sport: String,
    val workoutProvider: String,
    val workoutSourceId: String,
    val steps: List<Step>
)

@Serializable
data class Step(
    val type: String,
    val stepOrder: Int,
    val repeatValue: Int? = null,
    val intensity: Intensity,
    val description: String = "",
    val durationType: DurationType,
    val durationValue: Float = 0.0f,
    val targetType: TargetType? = null,
    val targetValue: Float = 0.0f,
    val targetValueLow: Float? = null,
    val targetValueHigh: Float? = null,
)

enum class Intensity(val value: String) {
    WARMUP("WARMUP"),
    ACTIVE("ACTIVE"),
    INTERVAL("INTERVAL"),
    RECOVERY("RECOVERY"),
    REST("REST"),
    COOLDOWN("COOLDOWN")
}

enum class DurationType(val value: String) {
    TIME("TIME"),
    DISTANCE("DISTANCE"),
    OPEN("OPEN")
}

enum class TargetType(val value: String) {
    SPEED("SPEED"),
    OPEN("OPEN"),
    HEARTBEAT("HEARTBEAT"),
}

