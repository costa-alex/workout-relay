package io.github.costaalex.workoutrelay.app.workout

import config.BaseSpringITConfig
import org.assertj.core.api.Assertions.assertThat
import io.github.costaalex.workoutrelay.app.workout.schedule.C2CScheduledRequest
import io.github.costaalex.workoutrelay.app.workout.schedule.WorkoutScheduledJob
import io.github.costaalex.workoutrelay.domain.Platform
import io.github.costaalex.workoutrelay.domain.TrainingType
import io.github.costaalex.workoutrelay.infrastructure.schedule.ScheduleRequestEntity
import io.github.costaalex.workoutrelay.infrastructure.schedule.ScheduleRequestRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class WorkoutJobSchedulerIT : BaseSpringITConfig() {
    @Autowired
    lateinit var workoutScheduledJob: WorkoutScheduledJob

    @Autowired
    lateinit var scheduleRequestRepository: ScheduleRequestRepository

    @BeforeEach
    fun clearSchedules() {
        scheduleRequestRepository.deleteAll()
    }

    @Test
    fun `stores the configured rolling period`() {
        val request = C2CScheduledRequest(
            types = listOf(TrainingType.BIKE),
            skipSynced = true,
            sourcePlatform = Platform.INTERVALS,
            targetPlatform = Platform.TRAINING_PEAKS,
            startOffsetDays = -1,
            endOffsetDays = 2
        )

        workoutScheduledJob.addRequest(request)

        val savedRequest = workoutScheduledJob.getRequests().single()

        assertThat(savedRequest.types).containsExactly(TrainingType.BIKE)
        assertThat(savedRequest.skipSynced).isTrue()
        assertThat(savedRequest.sourcePlatform).isEqualTo(Platform.INTERVALS)
        assertThat(savedRequest.targetPlatform).isEqualTo(Platform.TRAINING_PEAKS)
        assertThat(savedRequest.startOffsetDays).isEqualTo(-1)
        assertThat(savedRequest.endOffsetDays).isEqualTo(2)
    }

    @Test
    fun `loads existing current-day schedules without offset fields`() {
        scheduleRequestRepository.save(
            ScheduleRequestEntity(
                """{
                    "types":["BIKE"],
                    "skipSynced":true,
                    "sourcePlatform":"INTERVALS",
                    "targetPlatform":"TRAINING_PEAKS"
                }""".trimIndent()
            )
        )

        val savedRequest = workoutScheduledJob.getRequests().single()

        assertThat(savedRequest.startOffsetDays).isZero()
        assertThat(savedRequest.endOffsetDays).isZero()
    }
}
