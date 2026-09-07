package io.github.costaalex.workoutrelay.app.activity

import io.github.costaalex.workoutrelay.domain.Platform
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ActivityServiceTest {

    @Test
    fun `should fail with clear error when repository is missing`() {
        val activityService =
            ActivityService(emptyList())

        val request =
            CopyActivitiesRequest(
                sourcePlatform =
                    Platform.TRAINER_ROAD,
                targetPlatform =
                    Platform.INTERVALS,
                startDate =
                    java.time.LocalDate.of(
                        2026,
                        7,
                        1,
                    ),
                endDate =
                    java.time.LocalDate.of(
                        2026,
                        7,
                        2,
                    ),
                types = emptyList(),
            )

        val exception =
            assertThrows<IllegalStateException> {
                activityService.syncActivities(request)
            }

        assertEquals(
            "No ActivityRepository registered " +
                "for platform TRAINER_ROAD",
            exception.message,
        )
    }

    @Test
    fun `should reject unsupported activity direction`() {
        val activityService = ActivityService(emptyList())
        val request = CopyActivitiesRequest(
            sourcePlatform = Platform.INTERVALS,
            targetPlatform = Platform.TRAINER_ROAD,
            startDate = java.time.LocalDate.of(2026, 7, 1),
            endDate = java.time.LocalDate.of(2026, 7, 2),
            types = emptyList(),
        )

        val exception = assertThrows<IllegalArgumentException> {
            activityService.syncActivities(request)
        }

        assertEquals(
            "Activity synchronization from INTERVALS to TRAINER_ROAD is not supported",
            exception.message,
        )
    }
}