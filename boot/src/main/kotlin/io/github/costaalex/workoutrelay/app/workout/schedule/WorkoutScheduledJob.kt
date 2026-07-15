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

@Service
class WorkoutScheduledJob(
    private val syncExecutionService: SyncExecutionService,
    private val scheduleRequestRepository: ScheduleRequestRepository,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

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
                    startOffsetDays =
                        request.startOffsetDays,
                    endOffsetDays =
                        request.endOffsetDays
                )
            }

    fun deleteRequest(id: Int) {
        require(scheduleRequestRepository.existsById(id)) {
            "Scheduled sync $id does not exist"
        }

        scheduleRequestRepository.deleteById(id)
    }

    fun runRequest(
        id: Int
    ): CopyWorkoutsResponse {
        val entity = scheduleRequestRepository
            .findById(id)
            .orElseThrow {
                IllegalArgumentException(
                    "Scheduled sync $id does not exist"
                )
            }

        val scheduledRequest =
            entity.tryToSchedulable()
                ?: throw IllegalStateException(
                    "Scheduled sync $id contains an " +
                        "invalid configuration"
                )

        return syncExecutionService.execute(
            request = scheduledRequest.toCopyRequest(),
            trigger = SyncExecutionTrigger.RUN_NOW,
            scheduleId = entity.id
        )
    }

    @Scheduled(
        fixedRateString =
            "\${app.scheduler.interval-hours}",
        timeUnit = TimeUnit.HOURS
    )
    fun job() {
        val entities = scheduleRequestRepository
            .findAll()
            .toList()

        log.info(
            "Starting scheduled sync processing. requests={}",
            entities.size
        )

        entities.forEach { entity ->
            val scheduledRequest =
                entity.tryToSchedulable()
                    ?: return@forEach

            try {
                syncExecutionService.execute(
                    request =
                        scheduledRequest.toCopyRequest(),
                    trigger =
                        SyncExecutionTrigger.SCHEDULED,
                    scheduleId = entity.id
                )
            } catch (exception: Exception) {
                log.error(
                    "Scheduled sync {} failed",
                    entity.id,
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
                "Ignoring invalid scheduled sync. " +
                    "id={}, reason={}",
                id,
                exception.message
                    ?: exception.javaClass.simpleName
            )

            null
        }
    }
}
