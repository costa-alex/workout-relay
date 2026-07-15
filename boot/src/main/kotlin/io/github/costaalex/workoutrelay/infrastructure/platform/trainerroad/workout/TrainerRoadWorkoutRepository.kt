package io.github.costaalex.workoutrelay.infrastructure.platform.trainerroad.workout

import io.github.costaalex.workoutrelay.domain.ExternalData
import io.github.costaalex.workoutrelay.domain.Platform
import io.github.costaalex.workoutrelay.domain.librarycontainer.LibraryContainer
import io.github.costaalex.workoutrelay.domain.workout.Workout
import io.github.costaalex.workoutrelay.domain.workout.WorkoutDetails
import io.github.costaalex.workoutrelay.domain.workout.WorkoutRepository
import io.github.costaalex.workoutrelay.infrastructure.PlatformException
import io.github.costaalex.workoutrelay.infrastructure.platform.trainerroad.TrainerRoadApiClientService
import io.github.costaalex.workoutrelay.infrastructure.platform.trainerroad.member.TRUsernameRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate


@Repository
class TrainerRoadWorkoutRepository(
    private val trUsernameRepository: TRUsernameRepository,
    private val trainerRoadApiClientService: TrainerRoadApiClientService,
) : WorkoutRepository {
    override fun platform() = Platform.TRAINER_ROAD

    override fun getWorkoutFromLibrary(externalData: ExternalData): Workout {
        return trainerRoadApiClientService.getWorkout(externalData.trainerRoadId!!)
    }

    override fun findWorkoutsFromLibraryByName(name: String): List<WorkoutDetails> {
        return trainerRoadApiClientService.findWorkoutsFromLibraryByName(name)
    }

    override fun getWorkoutsFromCalendar(startDate: LocalDate, endDate: LocalDate): List<Workout> {
        val memberId = trUsernameRepository.getMemberId()
        return trainerRoadApiClientService.getWorkoutsFromCalendar(startDate, endDate, memberId)
    }

    override fun saveWorkoutsToCalendar(workouts: List<Workout>) {
        throw PlatformException(Platform.TRAINER_ROAD, "TR doesn't support workout planning")
    }

    override fun saveWorkoutsToLibrary(libraryContainer: LibraryContainer, workouts: List<Workout>) {
        throw PlatformException(Platform.TRAINER_ROAD, "TR doesn't support workout creation")
    }

    override fun getWorkoutsFromLibrary(libraryContainer: LibraryContainer): List<Workout> {
        throw PlatformException(Platform.TRAINER_ROAD, "TR has only one library, search by name")
    }

    override fun deleteWorkoutsFromCalendar(startDate: LocalDate, endDate: LocalDate) {
        TODO("Not implemented")
    }

}
