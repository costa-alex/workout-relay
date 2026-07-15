package io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.workout

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.costaalex.workoutrelay.domain.ExternalData
import io.github.costaalex.workoutrelay.domain.Platform
import io.github.costaalex.workoutrelay.domain.librarycontainer.LibraryContainer
import io.github.costaalex.workoutrelay.domain.workout.Workout
import io.github.costaalex.workoutrelay.domain.workout.WorkoutDetails
import io.github.costaalex.workoutrelay.domain.workout.WorkoutRepository
import io.github.costaalex.workoutrelay.domain.workout.structure.SingleStep
import io.github.costaalex.workoutrelay.infrastructure.PlatformException
import io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.TrainingPeaksApiClient
import io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.configuration.TrainingPeaksConfigurationRepository
import io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.library.TPWorkoutLibraryRepository
import io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.plan.TrainingPeaksPlanCoachApiClient
import io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.plan.TrainingPeaksPlanRepository
import io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.user.TrainingPeaksUserRepository
import io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.workout.structure.ToTPStructureConverter
import io.github.costaalex.workoutrelay.infrastructure.utils.Date
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository
import java.time.LocalDate


@CacheConfig(cacheNames = ["tpWorkoutsCache"])
@Repository
class TrainingPeaksWorkoutRepository(
    private val trainingPeaksApiClient: TrainingPeaksApiClient,
    private val trainingPeaksPlanCoachApiClient: TrainingPeaksPlanCoachApiClient,
    private val tpToWorkoutConverter: TPToWorkoutConverter,
    private val trainingPeaksPlanRepository: TrainingPeaksPlanRepository,
    private val trainingPeaksUserRepository: TrainingPeaksUserRepository,
    private val tpWorkoutLibraryRepository: TPWorkoutLibraryRepository,
    private val tpAttachmentService: TPAttachmentService,
    private val trainingPeaksConfigurationRepository: TrainingPeaksConfigurationRepository,
    private val objectMapper: ObjectMapper,
) : WorkoutRepository {
    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun platform() = Platform.TRAINING_PEAKS

    override fun saveWorkoutsToCalendar(workouts: List<Workout>) {
        workouts.forEach { saveWorkoutToCalendar(it) }
    }

    @Cacheable(key = "#libraryContainer.externalData.trainingPeaksId")
    override fun getWorkoutsFromLibrary(libraryContainer: LibraryContainer): List<Workout> {
        val user = trainingPeaksUserRepository.getUser()
        return if (libraryContainer.isPlan) {
            if (user.isAthlete) {
                getWorkoutsFromTPPlan(libraryContainer)
            } else {
                getWorkoutsFromTPCoachPlan(libraryContainer)
            }
        } else {
            getWorkoutsFromTPLibrary(libraryContainer)
        }
    }

    private fun getWorkoutsFromTPCoachPlan(libraryContainer: LibraryContainer): List<Workout> {
        val planWorkouts = trainingPeaksPlanCoachApiClient.getPlanWorkouts(
            libraryContainer.externalData.trainingPeaksId!!,
            libraryContainer.startDate.minusYears(10).toString(),
            libraryContainer.startDate.plusYears(2).toString()
        )
        val planNotes = trainingPeaksPlanCoachApiClient.getPlanNotes(
            libraryContainer.externalData.trainingPeaksId,
            libraryContainer.startDate.minusYears(10).toString(),
            libraryContainer.startDate.plusYears(2).toString()
        )

        val workouts = planWorkouts.map {
            tpToWorkoutConverter.toWorkout(it)
        }

        val notes = planNotes.map { tpToWorkoutConverter.toWorkout(it) }
        return workouts + notes
    }

    override fun getWorkoutsFromCalendar(startDate: LocalDate, endDate: LocalDate): List<Workout> {
        val userId = trainingPeaksUserRepository.getUser().userId
        val tpWorkouts = trainingPeaksApiClient.getWorkouts(userId, startDate.toString(), endDate.toString())

        val noteEndDate = getNoteEndDateForFilter(startDate, endDate)
        val tpNotes = trainingPeaksApiClient.getNotes(userId, startDate.toString(), noteEndDate.toString())
        val workouts = tpWorkouts.map {
            val attachments = tpAttachmentService.getAttachments(userId, it.id)
            tpToWorkoutConverter.toWorkout(it, attachments)
        }

        val notes = tpNotes.map { tpToWorkoutConverter.toWorkout(it) }
        return workouts + notes
    }

    override fun findWorkoutsFromLibraryByName(name: String): List<WorkoutDetails> {
        return tpWorkoutLibraryRepository.getAllWorkouts()
            .map { it.details }
            .filter { it.name.contains(name) }
    }

    override fun getWorkoutFromLibrary(externalData: ExternalData): Workout {
        return tpWorkoutLibraryRepository.getAllWorkouts()
            .find { it.details.externalData == externalData }!!
    }

    override fun saveWorkoutsToLibrary(libraryContainer: LibraryContainer, workouts: List<Workout>) {
        throw PlatformException(Platform.TRAINING_PEAKS, "TP doesn't support workout creation")
    }

    override fun deleteWorkoutsFromCalendar(
        startDate: LocalDate,
        endDate: LocalDate
    ) {
        getWorkoutsFromCalendar(startDate, endDate)
            .filter(::isApplicationManagedTrainerRoadWorkout)
            .forEach(::deleteWorkoutFromCalendar)
    }

    override fun saveWorkoutToCalendar(workout: Workout) {
        val structureStr = workout.structure?.let {
            ToTPStructureConverter.toStructureString(objectMapper, it)
        }

        val athleteId = trainingPeaksUserRepository.getUser().userId

        val createRequest = CreateTPWorkoutRequestDTO.planWorkout(
            athleteId = athleteId,
            workout = workout,
            structureStr = structureStr
        )

        log.info(
            "Creating TP workout '{}'. type={}, subType={}, " +
                "workoutTypeValueId={}, workoutSubTypeId={}",
            workout.details.name,
            workout.details.type,
            workout.details.subType,
            createRequest.workoutTypeValueId,
            createRequest.workoutSubTypeId
        )

        trainingPeaksApiClient.createAndPlanWorkout(
            athleteId,
            createRequest
        )
    }

    private fun getWorkoutsFromTPPlan(libraryContainer: LibraryContainer): List<Workout> {
        val planId = libraryContainer.externalData.trainingPeaksId!!
        val planStartDateShift = trainingPeaksConfigurationRepository.getConfiguration().planDaysShift
        val planStartDate = Date.thisMonday()
        val planApplyDate = planStartDate.plusDays(planStartDateShift)
        val response = trainingPeaksPlanRepository.applyPlan(planId, planApplyDate)

        try {
            val planEndDate = response.endDate.toLocalDate()

            val workouts = getWorkoutsFromCalendar(planApplyDate, planEndDate).map {
                it.withDate(it.date!!.minusDays(planStartDateShift))
            }
            return workouts
        } catch (e: Exception) {
            throw e
        } finally {
            trainingPeaksPlanRepository.removeAppliedPlan(response.appliedPlanId)
        }
    }

    private fun getWorkoutsFromTPLibrary(library: LibraryContainer): List<Workout> {
        return tpWorkoutLibraryRepository.getLibraryWorkouts(library.externalData.trainingPeaksId!!)
    }

    private fun getNoteEndDateForFilter(startDate: LocalDate, endDate: LocalDate): LocalDate =
        if (startDate == endDate) endDate.plusDays(1) else endDate

    private fun targetPreview(workout: Workout): String {
        return workout.structure?.steps
            ?.filterIsInstance<SingleStep>()
            ?.take(8)
            ?.joinToString { "${it.name}:${it.target.start}-${it.target.end}" }
            ?: "no structure"
    }
    
    override fun deleteWorkoutFromCalendar(workout: Workout) {
        val externalData = workout.details.externalData

        val trainingPeaksId = externalData.trainingPeaksId
            ?: throw IllegalArgumentException(
                "Cannot delete workout without TrainingPeaks ID"
            )

        require(externalData.trainerRoadId != null) {
            "Refusing to delete a workout without TrainerRoad metadata"
        }

        require(
            workout.details.description?.contains(
                ExternalData.DESCRIPTION_SEPARATOR
            ) == true
        ) {
            "Refusing to delete a workout not managed by the application"
        }

        val userId = trainingPeaksUserRepository.getUser().userId

        log.info(
            "Deleting application-managed TrainingPeaks workout '{}'. " +
                "trainingPeaksId={}, trainerRoadId={}",
            workout.details.name,
            trainingPeaksId,
            externalData.trainerRoadId
        )

        trainingPeaksApiClient.deleteWorkout(
            userId,
            trainingPeaksId
        )
    }
    
    private fun isApplicationManagedTrainerRoadWorkout(
        workout: Workout
    ): Boolean {
        val externalData = workout.details.externalData

        return externalData.trainingPeaksId != null &&
            externalData.trainerRoadId != null &&
            workout.details.description?.contains(
                ExternalData.DESCRIPTION_SEPARATOR
            ) == true
    }

}
