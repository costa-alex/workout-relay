package io.github.costaalex.workoutrelay.app.workout

import io.github.costaalex.workoutrelay.domain.ExternalData
import java.time.LocalDate

data class CopyWorkoutsResponse(
    val copied: Int,
    val filteredOut: Int,
    val skippedByType: Int,
    val skippedAlreadySynced: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val externalData: ExternalData,
    val failed: Int = 0,
    val failedWorkouts: List<WorkoutSyncFailure> = emptyList(),
    val removed: Int = 0,
    val failedToRemove: Int = 0,
    val failedRemovals: List<WorkoutSyncFailure> = emptyList()
)