package org.hans.model

import java.time.LocalDate

data class Workout(
    val name: String?,
    val description: String?,
    val steps: Any?,
    val date: LocalDate?,
    val id: String?,
    val duration: Float?,
    val distance: Float?
)
