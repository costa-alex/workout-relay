package io.github.costaalex.workoutrelay.app.workout

import io.github.costaalex.workoutrelay.domain.Platform
import io.github.costaalex.workoutrelay.domain.TrainingType
import java.time.LocalDate

data class CopyFromCalendarToLibraryRequest(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val name: String,
    val isPlan: Boolean,
    val types: List<TrainingType>,
    val sourcePlatform: Platform,
    val targetPlatform: Platform
)
