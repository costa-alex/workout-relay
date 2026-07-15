package io.github.costaalex.workoutrelay.app.workout.execution

import io.github.costaalex.workoutrelay.infrastructure.configuration.SyncHistoryProperties
import io.github.costaalex.workoutrelay.infrastructure.sync.SyncExecutionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class SyncExecutionMaintenanceService(
    private val syncExecutionRepository: SyncExecutionRepository,
    private val syncHistoryProperties: SyncHistoryProperties
) {

    private val log =
        LoggerFactory.getLogger(this.javaClass)

    @Transactional
    fun recoverInterruptedExecutions(): Int {
        val runningExecutions =
            syncExecutionRepository.findAllByStatus(
                SyncExecutionStatus.RUNNING
            )

        if (runningExecutions.isEmpty()) {
            return 0
        }

        val interruptedAt = LocalDateTime.now()

        runningExecutions.forEach { execution ->
            execution.status =
                SyncExecutionStatus.INTERRUPTED

            execution.finishedAt = interruptedAt

            execution.errorMessage =
                "Execution interrupted because the " +
                    "application stopped before it completed."
        }

        syncExecutionRepository.saveAll(
            runningExecutions
        )

        log.warn(
            "Marked {} unfinished sync executions as interrupted",
            runningExecutions.size
        )

        return runningExecutions.size
    }

    @Transactional
    fun enforceRetention(): Int {
        val deleted =
            syncExecutionRepository.deleteAllExceptLatest(
                syncHistoryProperties.retentionLimit
            )

        if (deleted > 0) {
            log.info(
                "Deleted {} old sync executions. " +
                    "Retention limit={}",
                deleted,
                syncHistoryProperties.retentionLimit
            )
        }

        return deleted
    }
}