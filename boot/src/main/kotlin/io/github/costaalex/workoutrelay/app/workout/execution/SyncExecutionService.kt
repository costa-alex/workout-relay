package io.github.costaalex.workoutrelay.app.workout.execution

import io.github.costaalex.workoutrelay.app.workout.CopyFromCalendarToCalendarRequest
import io.github.costaalex.workoutrelay.app.workout.CopyWorkoutsResponse
import io.github.costaalex.workoutrelay.app.workout.WorkoutService
import io.github.costaalex.workoutrelay.infrastructure.sync.SyncExecutionEntity
import io.github.costaalex.workoutrelay.infrastructure.sync.SyncExecutionRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import io.github.costaalex.workoutrelay.infrastructure.configuration.SyncHistoryProperties
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest

@Service
class SyncExecutionService(
    private val workoutService: WorkoutService,
    private val syncExecutionRepository: SyncExecutionRepository,
    private val maintenanceService: SyncExecutionMaintenanceService,
    private val syncHistoryProperties: SyncHistoryProperties
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    fun execute(
        request: CopyFromCalendarToCalendarRequest,
        trigger: SyncExecutionTrigger,
        scheduleId: Int? = null
    ): CopyWorkoutsResponse {

        val execution = syncExecutionRepository.save(
            SyncExecutionEntity(
                scheduleId = scheduleId,
                triggerType = trigger,
                sourcePlatform = request.sourcePlatform,
                targetPlatform = request.targetPlatform,
                startDate = request.startDate,
                endDate = request.endDate,
                startedAt = LocalDateTime.now(),
                status = SyncExecutionStatus.RUNNING
            )
        )

        try {
            val response =
                workoutService.copyWorkoutsC2C(request)

            execution.finishedAt = LocalDateTime.now()
            execution.copied = response.copied
            execution.removed = response.removed
            execution.skippedByType =
                response.skippedByType
            execution.skippedAlreadySynced =
                response.skippedAlreadySynced
            execution.failed = response.failed
            execution.failedToRemove =
                response.failedToRemove

            execution.errorMessage = (
                response.failedWorkouts.map { failure ->
                    "${failure.workoutName}: ${failure.message}"
                } +
                    response.failedRemovals.map { failure ->
                        "${failure.workoutName}: ${failure.message}"
                    }
                )
                .takeIf { it.isNotEmpty() }
                ?.joinToString("\n")
                ?.take(2000)

            execution.status = determineStatus(response)

            syncExecutionRepository.save(execution)

            return response
        } catch (exception: Exception) {
            execution.finishedAt = LocalDateTime.now()
            execution.status = SyncExecutionStatus.FAILED
            execution.errorMessage =
                exception.message
                    ?.take(2000)
                    ?: exception.javaClass.simpleName

            syncExecutionRepository.save(execution)

            throw exception
        } finally {
            try {
                maintenanceService.enforceRetention()
            } catch (exception: Exception) {
                /*
                * Uma falha de manutenção do histórico não deve
                * alterar o resultado da sincronização.
                */
                log.error(
                    "Unable to enforce sync history retention",
                    exception
                )
            }
        }
    }

    fun getRecentExecutions():
        List<SyncExecutionResponse> {

        val pageable = PageRequest.of(
            0,
            syncHistoryProperties.retentionLimit
        )

        return syncExecutionRepository
            .findAllByOrderByIdDesc(pageable)
            .map(SyncExecutionResponse::fromEntity)
    }

    private fun determineStatus(
        response: CopyWorkoutsResponse
    ): SyncExecutionStatus {

        val hasFailures =
            response.failed > 0 ||
                response.failedToRemove > 0

        val hasSuccessfulChanges =
            response.copied > 0 ||
                response.removed > 0

        return when {
            hasFailures && !hasSuccessfulChanges ->
                SyncExecutionStatus.FAILED

            hasFailures ->
                SyncExecutionStatus.PARTIAL_SUCCESS

            !hasSuccessfulChanges ->
                SyncExecutionStatus.NO_CHANGES

            else ->
                SyncExecutionStatus.SUCCESS
        }
    }
}