package org.freekode.tp2intervals.app.workout.schedule

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType

data class ScheduledSyncResponse(
    val id: Int,
    val types: List<TrainingType>,
    val skipSynced: Boolean,
    val sourcePlatform: Platform,
    val targetPlatform: Platform,
    val startOffsetDays: Int,
    val endOffsetDays: Int
)
