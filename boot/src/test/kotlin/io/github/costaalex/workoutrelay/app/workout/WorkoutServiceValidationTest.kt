package io.github.costaalex.workoutrelay.app.workout

import io.github.costaalex.workoutrelay.domain.Platform
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.Test
import java.time.LocalDate

class WorkoutServiceValidationTest {
    private val service = WorkoutService(emptyList(), emptyList())

    @Test
    fun `rejects an inverted calendar range before accessing repositories`() {
        val request = CopyFromCalendarToCalendarRequest(
            startDate = LocalDate.of(2026, 9, 2),
            endDate = LocalDate.of(2026, 9, 1),
            types = emptyList(),
            skipSynced = true,
            sourcePlatform = Platform.TRAINER_ROAD,
            targetPlatform = Platform.INTERVALS,
        )

        assertThatIllegalArgumentException()
            .isThrownBy { service.copyWorkoutsC2C(request) }
            .withMessage("Start date cannot be after end date")
    }

    @Test
    fun `rejects an unsupported calendar direction before accessing repositories`() {
        val request = CopyFromCalendarToCalendarRequest(
            startDate = LocalDate.of(2026, 9, 1),
            endDate = LocalDate.of(2026, 9, 1),
            types = emptyList(),
            skipSynced = true,
            sourcePlatform = Platform.INTERVALS,
            targetPlatform = Platform.TRAINER_ROAD,
        )

        assertThatIllegalArgumentException()
            .isThrownBy { service.copyWorkoutsC2C(request) }
            .withMessage(
                "Calendar synchronization from INTERVALS to TRAINER_ROAD is not supported"
            )
    }

    @Test
    fun `rejects library search for an unsupported platform`() {
        assertThatIllegalArgumentException()
            .isThrownBy {
                service.findWorkoutsByName(
                    Platform.INTERVALS,
                    "threshold",
                )
            }
            .withMessage(
                "Workout library search is not supported for INTERVALS"
            )
    }
}