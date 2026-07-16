package io.github.costaalex.workoutrelay.app.workout.schedule

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.costaalex.workoutrelay.app.workout.CopyWorkoutsResponse
import io.github.costaalex.workoutrelay.app.workout.execution.SyncExecutionService
import io.github.costaalex.workoutrelay.app.workout.execution.SyncExecutionTrigger
import io.github.costaalex.workoutrelay.infrastructure.schedule.ScheduleRequestEntity
import io.github.costaalex.workoutrelay.infrastructure.schedule.ScheduleRequestRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import java.util.concurrent.ConcurrentHashMap

@Service
class WorkoutScheduledJob(
    private val syncExecutionService: SyncExecutionService,
    private val scheduleRequestRepository: ScheduleRequestRepository,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    private val runningScheduleIds = ConcurrentHashMap.newKeySet<Int>()

    fun addRequest(request: C2CScheduledRequest) {
        val alreadyExists = scheduleRequestRepository
            .findAll()
            .asSequence()
            .mapNotNull { entity ->
                entity.tryToSchedulable()
            }
            .any { existingRequest ->
                existingRequest == request
            }

        require(!alreadyExists) {
            "This scheduled sync already exists"
        }

        scheduleRequestRepository.save(
            ScheduleRequestEntity(
                objectMapper.writeValueAsString(request)
            )
        )
    }

    fun getRequests(): List<ScheduledSyncResponse> =
        scheduleRequestRepository
            .findAll()
            .mapNotNull { entity ->
                val request =
                    entity.tryToSchedulable()
                        ?: return@mapNotNull null

                ScheduledSyncResponse(
                    id = requireNotNull(entity.id),
                    types = request.types,
                    skipSynced = request.skipSynced,
                    sourcePlatform = request.sourcePlatform,
                    targetPlatform = request.targetPlatform,
                    startOffsetDays = request.startOffsetDays,
                    endOffsetDays = request.endOffsetDays
                )
            }

    fun deleteRequest(id: Int) {
        withScheduleLock(id) {
            require(
                scheduleRequestRepository.existsById(id)
            ) {
                "Scheduled sync $id does not exist"
            }

            scheduleRequestRepository.deleteById(id)
        }
    }

    fun runRequest(
        id: Int
    ): CopyWorkoutsResponse {
        return executeSchedule(
            scheduleId = id,
            trigger = SyncExecutionTrigger.RUN_NOW
        )
    }

    @Scheduled(
        fixedRateString = "\${app.scheduler.interval-hours}",
        timeUnit = TimeUnit.HOURS
    )
    fun job() {
        val scheduleIds = scheduleRequestRepository
            .findAll()
            .mapNotNull { entity ->
                entity.id
            }

        log.info(
            "Starting scheduled sync processing. requests={}",
            scheduleIds.size
        )

        scheduleIds.forEach { scheduleId ->
            try {
                executeSchedule(
                    scheduleId = scheduleId,
                    trigger = SyncExecutionTrigger.SCHEDULED
                )
            } catch (
                exception: ScheduleAlreadyRunningException
            ) {
                log.warn(
                    "Skipping scheduled sync {} because it is already running",
                    scheduleId
                )
            } catch (exception: Exception) {
                log.error(
                    "Scheduled sync {} failed",
                    scheduleId,
                    exception
                )
            }
        }

        log.info("Finished scheduled sync processing")
    }

    private fun ScheduleRequestEntity.tryToSchedulable():
        C2CScheduledRequest? {

        return try {
            objectMapper.readValue(
                requireNotNull(requestJson) {
                    "Scheduled sync request JSON is missing"
                },
                C2CScheduledRequest::class.java
            )
        } catch (exception: Exception) {
            log.error(
                "Ignoring invalid scheduled sync. id={}, reason={}",
                id,
                exception.message
                    ?: exception.javaClass.simpleName
            )

            null
        }
    }

    private fun executeSchedule(
        scheduleId: Int,
        trigger: SyncExecutionTrigger
    ): CopyWorkoutsResponse {
        return withScheduleLock(scheduleId) {
            val entity = scheduleRequestRepository
                .findById(scheduleId)
                .orElseThrow {
                    IllegalArgumentException("Scheduled sync $scheduleId does not exist")
                }

            val scheduledRequest =
                entity.tryToSchedulable()
                    ?: throw IllegalStateException("Scheduled sync $scheduleId contains an invalid configuration")

            log.info(
                "Starting scheduled sync. id={}, trigger={}",
                scheduleId,
                trigger
            )

            try {
                syncExecutionService.execute(
                    request = scheduledRequest.toCopyRequest(),
                    trigger = trigger,
                    scheduleId = scheduleId
                )
            } finally {
                log.info(
                    "Scheduled sync execution finished. id={}, trigger={}",
                    scheduleId,
                    trigger
                )
            }
        }
    }

    private inline fun <T> withScheduleLock(
        scheduleId: Int,
        action: () -> T
    ): T {
        if (!runningScheduleIds.add(scheduleId)) {
            throw ScheduleAlreadyRunningException(
                scheduleId
            )
        }

        return try {
            action()
        } finally {
            runningScheduleIds.remove(scheduleId)
        }
    }
}
