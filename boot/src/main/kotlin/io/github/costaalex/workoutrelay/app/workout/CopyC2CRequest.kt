package io.github.costaalex.workoutrelay.app.workout

import io.github.costaalex.workoutrelay.domain.Platform
import io.github.costaalex.workoutrelay.domain.TrainingType
import java.time.LocalDate

data class CopyFromCalendarToCalendarRequest(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val types: List<TrainingType>,
    val skipSynced: Boolean,
    val sourcePlatform: Platform,
    val targetPlatform: Platform,
    val replaceChangedWorkouts: Boolean = false
)