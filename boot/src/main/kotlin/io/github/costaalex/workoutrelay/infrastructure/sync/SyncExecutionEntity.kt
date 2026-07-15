package io.github.costaalex.workoutrelay.infrastructure.sync

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import io.github.costaalex.workoutrelay.app.workout.execution.SyncExecutionStatus
import io.github.costaalex.workoutrelay.app.workout.execution.SyncExecutionTrigger
import io.github.costaalex.workoutrelay.domain.Platform
import java.time.LocalDateTime
import java.time.LocalDate
import jakarta.persistence.Convert

@Entity
@Table(name = "sync_executions")
class SyncExecutionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "schedule_id")
    var scheduleId: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false)
    var triggerType: SyncExecutionTrigger,

    @Enumerated(EnumType.STRING)
    @Column(name = "source_platform", nullable = false)
    var sourcePlatform: Platform,

    @Enumerated(EnumType.STRING)
    @Column(name = "target_platform", nullable = false)
    var targetPlatform: Platform,

    @Convert(
        converter = LocalDateTimeStringConverter::class
    )
    @Column(name = "started_at", nullable = false)
    var startedAt: LocalDateTime,

    @Convert(
        converter = LocalDateTimeStringConverter::class
    )
    @Column(name = "finished_at")
    var finishedAt: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: SyncExecutionStatus,

    var copied: Int = 0,

    var removed: Int = 0,

    @Column(name = "skipped_by_type")
    var skippedByType: Int = 0,

    @Column(name = "skipped_already_synced")
    var skippedAlreadySynced: Int = 0,

    var failed: Int = 0,

    @Column(name = "failed_to_remove")
    var failedToRemove: Int = 0,

    @Column(name = "error_message")
    var errorMessage: String? = null,
    
    @Column(name = "start_date", nullable = false)
    var startDate: LocalDate,

    @Column(name = "end_date", nullable = false)
    var endDate: LocalDate
) {
    constructor() : this(
        triggerType = SyncExecutionTrigger.MANUAL,
        sourcePlatform = Platform.GENERIC,
        targetPlatform = Platform.GENERIC,
        startedAt = LocalDateTime.now(),
        status = SyncExecutionStatus.RUNNING,
        startDate = LocalDate.now(),
        endDate = LocalDate.now()   
    )
}