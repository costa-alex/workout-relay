package io.github.costaalex.workoutrelay.infrastructure.platform.trainerroad.workout

import config.TestUtils
import io.github.costaalex.workoutrelay.domain.workout.structure.StepLength
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TrainerRoadWorkoutMapperTest {
    @Test
    fun `infers linear steps from workout data when interval data is empty`() {
        val response = TRWorkoutResponseDTO(
            TRWorkoutResponseDTO.TRWorkout(
                details = TrainerRoadWorkoutDetailsDTO(
                    id = "524179",
                    workoutName = "Lazy Mountain -1",
                    workoutDescription = "",
                    isOutside = false,
                    tss = 20.0,
                    duration = 60,
                ),
                intervalData = emptyList(),
                workoutData = (0..3600).map { tick ->
                    TRWorkoutResponseDTO.WorkoutDataPointDTO(
                        tick = tick,
                        ftpPercent = when (tick) {
                            in 0..600 -> 41.0 + 4.0 * tick / 600
                            in 601..3300 -> 45.0
                            else -> 45.0 - 3.0 * (tick - 3300) / 300
                        },
                    )
                },
            )
        )

        val workout = TrainerRoadWorkoutMapper().toWorkout(response, removeHtmlTags = false)

        val steps = workout.structure!!.steps
        assertThat(steps).hasSize(3)
        TestUtils.assertStep(steps[0], 600, StepLength.LengthUnit.SECONDS, 41, 45)
        TestUtils.assertStep(steps[1], 2700, StepLength.LengthUnit.SECONDS, 45, 45)
        TestUtils.assertStep(steps[2], 300, StepLength.LengthUnit.SECONDS, 45, 42)
    }
}