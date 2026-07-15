package io.github.costaalex.workoutrelay.app.workout

import java.time.LocalDate

data class WorkoutSyncFailure(
    val workoutName: String,
    val workoutDate: LocalDate?,
    val message: String
)