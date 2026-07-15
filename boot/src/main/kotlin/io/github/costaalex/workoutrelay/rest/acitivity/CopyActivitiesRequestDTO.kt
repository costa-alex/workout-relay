package io.github.costaalex.workoutrelay.rest.activity

import io.github.costaalex.workoutrelay.domain.Platform
import io.github.costaalex.workoutrelay.domain.TrainingType

class CopyActivitiesRequestDTO(
    val startDate: String,
    val endDate: String,
    val types: List<TrainingType>,
    val sourcePlatform: Platform,
    val targetPlatform: Platform
)
