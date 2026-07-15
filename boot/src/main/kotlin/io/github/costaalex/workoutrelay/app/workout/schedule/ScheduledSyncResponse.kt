package io.github.costaalex.workoutrelay.app.workout.schedule

import io.github.costaalex.workoutrelay.domain.Platform
import io.github.costaalex.workoutrelay.domain.TrainingType

data class ScheduledSyncResponse(
    val id: Int,
    val types: List<TrainingType>,
    val skipSynced: Boolean,
    val sourcePlatform: Platform,
    val targetPlatform: Platform,
    val startOffsetDays: Int,
    val endOffsetDays: Int
)
