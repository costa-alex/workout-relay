package org.freekode.tp2intervals.app.workout.schedule

import com.fasterxml.jackson.databind.ObjectMapper
import org.freekode.tp2intervals.app.workout.CopyWorkoutsResponse
import org.freekode.tp2intervals.app.workout.execution.SyncExecutionService
import org.freekode.tp2intervals.app.workout.execution.SyncExecutionTrigger
import org.freekode.tp2intervals.infrastructure.schedule.ScheduleRequestEntity
import org.freekode.tp2intervals.infrastructure.schedule.ScheduleRequestRepository
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
            .map { it.toSchedulable() }
            .any { it == request }

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
            .map { entity ->
                val request = entity.toSchedulable()

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
        require(scheduleRequestRepository.existsById(id)) {
            "Scheduled sync $id does not exist"
        }

        scheduleRequestRepository.deleteById(id)
    }

    fun runRequest(id: Int): CopyWorkoutsResponse {
        val entity = scheduleRequestRepository
            .findById(id)
            .orElseThrow {
                IllegalArgumentException(
                    "Scheduled sync $id does not exist"
                )
            }

        return syncExecutionService.execute(
            request = entity.toSchedulable().toCopyRequest(),
            trigger = SyncExecutionTrigger.RUN_NOW,
            scheduleId = entity.id
        )
    }

    @Scheduled(
        fixedRateString = "\${app.scheduler.interval-hours}",
        timeUnit = TimeUnit.HOURS
    )
    fun job() {
        val requests = scheduleRequestRepository
            .findAll()
            .toList()

        log.info(
            "Starting scheduled sync processing. requests={}",
            requests.size
        )

        requests.forEach { entity ->
            try {
                syncExecutionService.execute(
                    request = entity.toSchedulable().toCopyRequest(),
                    trigger = SyncExecutionTrigger.SCHEDULED,
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

    private fun ScheduleRequestEntity.toSchedulable():
        C2CScheduledRequest {

        return objectMapper.readValue(
            requireNotNull(requestJson),
            C2CScheduledRequest::class.java
        )
    }
}
