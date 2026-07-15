package io.github.costaalex.workoutrelay.rest.workout

import java.time.LocalDate
import io.github.costaalex.workoutrelay.domain.Platform

class DeleteWorkoutRequestDTO(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val platform: Platform,
)
