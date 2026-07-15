package io.github.costaalex.workoutrelay.app.workout.execution

import io.github.costaalex.workoutrelay.domain.Platform
import io.github.costaalex.workoutrelay.infrastructure.sync.SyncExecutionEntity
import java.time.LocalDateTime
import java.time.LocalDate

data class SyncExecutionResponse(
    val id: Int,
    val scheduleId: Int?,
    val triggerType: SyncExecutionTrigger,
    val sourcePlatform: Platform,
    val targetPlatform: Platform,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val startedAt: LocalDateTime,
    val finishedAt: LocalDateTime?,
    val status: SyncExecutionStatus,
    val copied: Int,
    val removed: Int,
    val skippedByType: Int,
    val skippedAlreadySynced: Int,
    val failed: Int,
    val failedToRemove: Int,
    val errorMessage: String?
) {
    companion object {
        fun fromEntity(
            entity: SyncExecutionEntity
        ) = SyncExecutionResponse(
            id = requireNotNull(entity.id),
            scheduleId = entity.scheduleId,
            triggerType = entity.triggerType,
            sourcePlatform = entity.sourcePlatform,
            targetPlatform = entity.targetPlatform,
            startDate = entity.startDate,
            endDate = entity.endDate,
            startedAt = entity.startedAt,
            finishedAt = entity.finishedAt,
            status = entity.status,
            copied = entity.copied,
            removed = entity.removed,
            skippedByType = entity.skippedByType,
            skippedAlreadySynced =
                entity.skippedAlreadySynced,
            failed = entity.failed,
            failedToRemove = entity.failedToRemove,
            errorMessage = entity.errorMessage
        )
    }
}