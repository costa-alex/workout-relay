package io.github.costaalex.workoutrelay.app.workout

import org.assertj.core.api.Assertions.assertThat
import io.github.costaalex.workoutrelay.domain.ExternalData
import io.github.costaalex.workoutrelay.domain.Platform
import io.github.costaalex.workoutrelay.domain.TrainingType
import io.github.costaalex.workoutrelay.domain.librarycontainer.LibraryContainer
import io.github.costaalex.workoutrelay.domain.workout.Workout
import io.github.costaalex.workoutrelay.domain.workout.WorkoutDetails
import io.github.costaalex.workoutrelay.domain.workout.WorkoutRepository
import org.junit.jupiter.api.Test
import java.time.LocalDate

class WorkoutServiceReconciliationRangeTest {

    @Test
    fun `reconciles TrainerRoad to TrainingPeaks one day at a time`() {
        val firstDate = LocalDate.of(2026, 7, 13)
        val secondDate = firstDate.plusDays(1)

        val sourceRepository = FakeWorkoutRepository(
            platform = Platform.TRAINER_ROAD,
            workoutsByDate = mapOf(
                firstDate to listOf(
                    workout(
                        date = firstDate,
                        name = "New first workout",
                        externalData = ExternalData(null, null, "tr-new-1")
                    )
                ),
                secondDate to listOf(
                    workout(
                        date = secondDate,
                        name = "New second workout",
                        externalData = ExternalData(null, null, "tr-new-2")
                    )
                )
            )
        )

        val targetRepository = FakeWorkoutRepository(
            platform = Platform.TRAINING_PEAKS,
            workoutsByDate = mapOf(
                firstDate to listOf(
                    workout(
                        date = firstDate,
                        name = "Old first workout",
                        description = ExternalData.DESCRIPTION_SEPARATOR,
                        externalData = ExternalData("tp-old-1", null, "tr-old-1")
                    )
                ),
                secondDate to listOf(
                    workout(
                        date = secondDate,
                        name = "Old second workout",
                        description = ExternalData.DESCRIPTION_SEPARATOR,
                        externalData = ExternalData("tp-old-2", null, "tr-old-2")
                    )
                )
            )
        )

        val service = WorkoutService(
            workoutRepositories = listOf(
                sourceRepository,
                targetRepository
            ),
            planRepositories = emptyList()
        )

        val response = service.copyWorkoutsC2C(
            CopyFromCalendarToCalendarRequest(
                startDate = firstDate,
                endDate = secondDate,
                types = listOf(TrainingType.BIKE),
                skipSynced = true,
                sourcePlatform = Platform.TRAINER_ROAD,
                targetPlatform = Platform.TRAINING_PEAKS,
                replaceChangedWorkouts = true
            )
        )

        assertThat(response.copied).isEqualTo(2)
        assertThat(response.removed).isEqualTo(2)
        assertThat(response.startDate).isEqualTo(firstDate)
        assertThat(response.endDate).isEqualTo(secondDate)
        assertThat(
            targetRepository.savedWorkouts.map {
                it.details.externalData.trainerRoadId
            }
        ).containsExactly("tr-new-1", "tr-new-2")
        assertThat(
            targetRepository.deletedWorkouts.map {
                it.details.externalData.trainerRoadId
            }
        ).containsExactly("tr-old-1", "tr-old-2")
        assertThat(sourceRepository.requestedPeriods)
            .containsExactly(
                firstDate to firstDate,
                secondDate to secondDate
            )
        assertThat(targetRepository.requestedPeriods)
            .containsExactly(
                firstDate to firstDate,
                secondDate to secondDate
            )
    }

    private fun workout(
        date: LocalDate,
        name: String,
        description: String? = null,
        externalData: ExternalData
    ) = Workout(
        details = WorkoutDetails(
            type = TrainingType.BIKE,
            subType = TrainingType.VIRTUAL_BIKE,
            name = name,
            description = description,
            duration = null,
            load = null,
            externalData = externalData
        ),
        date = date,
        structure = null
    )

    private class FakeWorkoutRepository(
        private val platform: Platform,
        private val workoutsByDate: Map<LocalDate, List<Workout>>
    ) : WorkoutRepository {
        val requestedPeriods = mutableListOf<Pair<LocalDate, LocalDate>>()
        val savedWorkouts = mutableListOf<Workout>()
        val deletedWorkouts = mutableListOf<Workout>()

        override fun platform() = platform

        override fun getWorkoutsFromCalendar(
            startDate: LocalDate,
            endDate: LocalDate
        ): List<Workout> {
            requestedPeriods += startDate to endDate
            return workoutsByDate[startDate].orEmpty()
        }

        override fun saveWorkoutsToCalendar(workouts: List<Workout>) {
            savedWorkouts += workouts
        }

        override fun deleteWorkoutFromCalendar(workout: Workout) {
            deletedWorkouts += workout
        }

        override fun getWorkoutsFromLibrary(
            libraryContainer: LibraryContainer
        ): List<Workout> = emptyList()

        override fun getWorkoutFromLibrary(
            externalData: ExternalData
        ): Workout = throw UnsupportedOperationException()

        override fun findWorkoutsFromLibraryByName(
            name: String
        ): List<WorkoutDetails> = emptyList()

        override fun saveWorkoutsToLibrary(
            libraryContainer: LibraryContainer,
            workouts: List<Workout>
        ) = Unit

        override fun deleteWorkoutsFromCalendar(
            startDate: LocalDate,
            endDate: LocalDate
        ) = Unit
    }
}
