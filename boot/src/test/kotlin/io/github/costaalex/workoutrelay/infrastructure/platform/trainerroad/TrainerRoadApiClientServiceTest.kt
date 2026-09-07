package io.github.costaalex.workoutrelay.infrastructure.platform.trainerroad

import io.github.costaalex.workoutrelay.domain.workout.Workout
import io.github.costaalex.workoutrelay.infrastructure.platform.trainerroad.activity.TrainerRoadActivityDTO
import io.github.costaalex.workoutrelay.infrastructure.platform.trainerroad.configuration.TrainerRoadConfigurationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime

class TrainerRoadApiClientServiceTest {
    private val trainerRoadApiClient = mock<TrainerRoadApiClient>()
    private val trainerRoadWorkoutCacheService = mock<TrainerRoadWorkoutCacheService>()
    private val service = TrainerRoadApiClientService(
        trainerRoadApiClient,
        mock<TrainerRoadConfigurationRepository>(),
        trainerRoadWorkoutCacheService,
    )

    @Test
    fun `should request an exclusive end date for a multi-day timeline`() {
        val startDate = LocalDate.of(2026, 9, 1)
        val endDate = LocalDate.of(2026, 9, 3)
        whenever(trainerRoadApiClient.getTimeline(42, "2026-09-01", "2026-09-04"))
            .thenReturn(TrainerRoadTimelineDTO())

        service.getWorkoutsFromCalendar(startDate, endDate, 42)

        verify(trainerRoadApiClient).getTimeline(42, "2026-09-01", "2026-09-04")
    }

    @Test
    fun `should reject an inverted timeline range`() {
        assertThrows<IllegalArgumentException> {
            service.getWorkoutsFromCalendar(
                LocalDate.of(2026, 9, 3),
                LocalDate.of(2026, 9, 1),
                42,
            )
        }

        verify(trainerRoadApiClient, never()).getTimeline(any(), any(), any())
    }

    @Test
    fun `should load planned workouts through the cache service and skip missing workout IDs`() {
        val date = LocalDate.of(2026, 9, 1)
        val cachedWorkout = mock<Workout>()
        val datedWorkout = mock<Workout>()
        whenever(trainerRoadApiClient.getTimeline(42, "2026-09-01", "2026-09-02"))
            .thenReturn(
                TrainerRoadTimelineDTO(
                    plannedActivities = listOf(
                        plannedActivity("missing", date, null),
                        plannedActivity("available", date, 7),
                    ),
                ),
            )
        whenever(trainerRoadWorkoutCacheService.getWorkout("7")).thenReturn(cachedWorkout)
        whenever(cachedWorkout.withDate(date)).thenReturn(datedWorkout)

        val workouts = service.getWorkoutsFromCalendar(date, date, 42)

        assertEquals(listOf(datedWorkout), workouts)
        verify(trainerRoadWorkoutCacheService).getWorkout("7")
    }

    @Test
    fun `should skip activity when its export ID is missing`() {
        val date = LocalDate.of(2026, 9, 1)
        whenever(trainerRoadApiClient.getTimeline(42, "2026-09-01", "2026-09-02"))
            .thenReturn(
                TrainerRoadTimelineDTO(
                    activities = listOf(
                        TrainerRoadTimelineDTO.ActivityStateDTO(11, date.atStartOfDay()),
                    ),
                ),
            )
        whenever(trainerRoadApiClient.getActivities(42, "11"))
            .thenReturn(listOf(activityWithoutExportId(date.atStartOfDay())))

        val activities = service.getActivities(42, date, date)

        assertEquals(emptyList<Any>(), activities)
        verify(trainerRoadApiClient, never()).exportFit(any())
    }

    private fun plannedActivity(
        id: String,
        date: LocalDate,
        workoutId: Long?,
    ) = TrainerRoadTimelineDTO.PlannedActivityDTO(
        id,
        TrainerRoadTimelineDTO.DateDTO(date.year, date.monthValue, date.dayOfMonth),
        workoutId,
    )

    private fun activityWithoutExportId(date: LocalDateTime) = TrainerRoadActivityDTO(
        Id = "activity-without-export-id",
        date = date,
        completedRide = null,
        activity = null,
        activityId = null,
        name = "Activity",
        isOutside = false,
        activityType = null,
    )
}