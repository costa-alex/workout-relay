package io.github.costaalex.workoutrelay.app.workout

import io.github.costaalex.workoutrelay.domain.ExternalData
import io.github.costaalex.workoutrelay.domain.Platform
import io.github.costaalex.workoutrelay.domain.librarycontainer.LibraryContainerRepository
import io.github.costaalex.workoutrelay.domain.workout.WorkoutDetails
import io.github.costaalex.workoutrelay.domain.workout.WorkoutRepository
import io.github.costaalex.workoutrelay.rest.workout.DeleteWorkoutRequestDTO
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import io.github.costaalex.workoutrelay.domain.workout.Workout

@Service
class WorkoutService(
    workoutRepositories: List<WorkoutRepository>,
    planRepositories: List<LibraryContainerRepository>,
) {
    private val log = LoggerFactory.getLogger(this.javaClass)
    private val workoutRepositoryMap = workoutRepositories.associateBy { it.platform() }
    private val planRepositoryMap = planRepositories.associateBy { it.platform() }

    private fun hasSameExternalId(source: ExternalData, target: ExternalData): Boolean {
        return (source.trainerRoadId != null && source.trainerRoadId == target.trainerRoadId)
            || (source.trainingPeaksId != null && source.trainingPeaksId == target.trainingPeaksId)
            || (source.intervalsId != null && source.intervalsId == target.intervalsId)
    }
    
    private data class DeleteWorkoutsResult(
        val removed: Int,
        val failures: List<WorkoutSyncFailure>
    )
    
    private data class SaveWorkoutsResult(
        val copied: Int,
        val failures: List<WorkoutSyncFailure>
    )
    
    private fun saveWorkoutsIndividually(
        repository: WorkoutRepository,
        workouts: List<Workout>
    ): SaveWorkoutsResult {

        var copied = 0
        val failures = mutableListOf<WorkoutSyncFailure>()

        workouts.forEach { workout ->
            try {
                repository.saveWorkoutToCalendar(workout)
                copied++
            } catch (exception: Exception) {
                log.error(
                    "Failed to sync workout '${workout.details.name}' " +
                        "on ${workout.date}",
                    exception
                )

                failures += WorkoutSyncFailure(
                    workoutName = workout.details.name,
                    workoutDate = workout.date,
                    message = exception.message
                        ?.lineSequence()
                        ?.firstOrNull()
                        ?.take(300)
                        ?: exception.javaClass.simpleName
                )
            }
        }

        return SaveWorkoutsResult(
            copied = copied,
            failures = failures
        )
    }
    
    private fun deleteWorkoutsIndividually(
        repository: WorkoutRepository,
        workouts: List<Workout>
    ): DeleteWorkoutsResult {

        var removed = 0
        val failures = mutableListOf<WorkoutSyncFailure>()

        workouts.forEach { workout ->
            try {
                repository.deleteWorkoutFromCalendar(workout)
                removed++
            } catch (exception: Exception) {
                log.error(
                    "Failed to remove replaced workout '${workout.details.name}' " +
                        "on ${workout.date}",
                    exception
                )

                failures += WorkoutSyncFailure(
                    workoutName = workout.details.name,
                    workoutDate = workout.date,
                    message = exception.message
                        ?.lineSequence()
                        ?.firstOrNull()
                        ?.take(300)
                        ?: exception.javaClass.simpleName
                )
            }
        }

        return DeleteWorkoutsResult(
            removed = removed,
            failures = failures
        )
    }
    
    fun copyWorkoutsC2C(
        request: CopyFromCalendarToCalendarRequest
    ): CopyWorkoutsResponse {

        return if (shouldReconcileChangedWorkouts(request)) {
            reconcileTrainerRoadToTrainingPeaksRange(request)
        } else {
            copyWorkoutsNormally(request)
        }
    }

    private fun reconcileTrainerRoadToTrainingPeaksRange(
        request: CopyFromCalendarToCalendarRequest
    ): CopyWorkoutsResponse {
        require(!request.startDate.isAfter(request.endDate)) {
            "Start date cannot be after end date"
        }

        val responses = mutableListOf<CopyWorkoutsResponse>()
        var currentDate = request.startDate

        while (!currentDate.isAfter(request.endDate)) {
            responses += reconcileTrainerRoadToTrainingPeaks(
                request.copy(
                    startDate = currentDate,
                    endDate = currentDate
                )
            )

            currentDate = currentDate.plusDays(1)
        }

        return CopyWorkoutsResponse(
            copied = responses.sumOf { it.copied },
            filteredOut = responses.sumOf { it.filteredOut },
            skippedByType = responses.sumOf { it.skippedByType },
            skippedAlreadySynced =
                responses.sumOf { it.skippedAlreadySynced },
            startDate = request.startDate,
            endDate = request.endDate,
            externalData = ExternalData.empty(),
            failed = responses.sumOf { it.failed },
            failedWorkouts = responses.flatMap { it.failedWorkouts },
            removed = responses.sumOf { it.removed },
            failedToRemove = responses.sumOf { it.failedToRemove },
            failedRemovals = responses.flatMap { it.failedRemovals }
        )
    }
    
    private fun reconcileTrainerRoadToTrainingPeaks(
        request: CopyFromCalendarToCalendarRequest
    ): CopyWorkoutsResponse {

        require(request.startDate == request.endDate) {
            "Workout reconciliation only supports a single day"
        }

        log.info(
            "Starting TrainerRoad to TrainingPeaks reconciliation for {}",
            request.startDate
        )

        val sourceRepository =
            workoutRepositoryMap[request.sourcePlatform]!!

        val targetRepository =
            workoutRepositoryMap[request.targetPlatform]!!

        val allSourceWorkouts =
            sourceRepository.getWorkoutsFromCalendar(
                request.startDate,
                request.endDate
            )

        val sourceWorkouts = allSourceWorkouts.filter {
            request.types.contains(it.details.type)
        }

        val skippedByType =
            allSourceWorkouts.size - sourceWorkouts.size

        /*
        * Medida de segurança:
        * se o TrainerRoad não devolver nenhum workout, não apagamos
        * nada do TrainingPeaks.
        */
        if (sourceWorkouts.isEmpty()) {
            log.info(
                "TrainerRoad returned no matching workouts for {}. " +
                    "No TrainingPeaks workouts will be removed.",
                request.startDate
            )

            return CopyWorkoutsResponse(
                copied = 0,
                filteredOut = skippedByType,
                skippedByType = skippedByType,
                skippedAlreadySynced = 0,
                startDate = request.startDate,
                endDate = request.endDate,
                externalData = ExternalData.empty()
            )
        }

        val sourceWithoutTrainerRoadId = sourceWorkouts.filter {
            it.details.externalData.trainerRoadId.isNullOrBlank()
        }

        val validSourceWorkouts = sourceWorkouts.filter {
            !it.details.externalData.trainerRoadId.isNullOrBlank()
        }

        val invalidSourceFailures =
            sourceWithoutTrainerRoadId.map { workout ->
                WorkoutSyncFailure(
                    workoutName = workout.details.name,
                    workoutDate = workout.date,
                    message = "TrainerRoad workout ID is missing"
                )
            }

        val targetWorkouts =
            targetRepository.getWorkoutsFromCalendar(
                request.startDate,
                request.endDate
            )

        val managedTargetWorkouts = targetWorkouts.filter {
            isApplicationManagedTrainerRoadWorkout(it)
        }

        val sourceTrainerRoadIds = validSourceWorkouts
            .mapNotNull {
                it.details.externalData.trainerRoadId
            }
            .toSet()

        val targetTrainerRoadIds = managedTargetWorkouts
            .mapNotNull {
                it.details.externalData.trainerRoadId
            }
            .toSet()

        val workoutsToCreate = validSourceWorkouts.filter {
            it.details.externalData.trainerRoadId !in targetTrainerRoadIds
        }

        val alreadySynced =
            validSourceWorkouts.size - workoutsToCreate.size

        val workoutsToRemove = managedTargetWorkouts.filter {
            it.details.externalData.trainerRoadId !in sourceTrainerRoadIds
        }

        /*
        * Primeiro criamos o workout novo.
        */
        val saveResult = saveWorkoutsIndividually(
            repository = targetRepository,
            workouts = workoutsToCreate
        )

        val copyFailures =
            invalidSourceFailures + saveResult.failures

        /*
        * Só apagamos os workouts anteriores se todos os novos
        * tiverem sido criados com sucesso.
        */
        val deleteResult = if (copyFailures.isEmpty()) {
            deleteWorkoutsIndividually(
                repository = targetRepository,
                workouts = workoutsToRemove
            )
        } else {
            log.warn(
                "Skipping removal of {} replaced workouts because " +
                    "{} workouts failed to sync",
                workoutsToRemove.size,
                copyFailures.size
            )

            DeleteWorkoutsResult(
                removed = 0,
                failures = emptyList()
            )
        }

        val response = CopyWorkoutsResponse(
            copied = saveResult.copied,
            filteredOut = skippedByType + alreadySynced,
            skippedByType = skippedByType,
            skippedAlreadySynced = alreadySynced,
            startDate = request.startDate,
            endDate = request.endDate,
            externalData = ExternalData.empty(),
            failed = copyFailures.size,
            failedWorkouts = copyFailures,
            removed = deleteResult.removed,
            failedToRemove = deleteResult.failures.size,
            failedRemovals = deleteResult.failures
        )

        log.info(
            "Reconciliation completed. date={}, copied={}, " +
                "alreadySynced={}, removed={}, failed={}, failedToRemove={}",
            request.startDate,
            response.copied,
            response.skippedAlreadySynced,
            response.removed,
            response.failed,
            response.failedToRemove
        )

        return response
    }
    
    private fun copyWorkoutsNormally(
        request: CopyFromCalendarToCalendarRequest
    ): CopyWorkoutsResponse {

        log.info("Received request for copy calendar to calendar: $request")

        val sourceWorkoutRepository =
            workoutRepositoryMap[request.sourcePlatform]!!

        val targetWorkoutRepository =
            workoutRepositoryMap[request.targetPlatform]!!

        val allWorkouts = sourceWorkoutRepository.getWorkoutsFromCalendar(
            request.startDate,
            request.endDate
        )

        val workoutsAfterTypeFilter = allWorkouts.filter {
            request.types.contains(it.details.type)
        }

        val skippedByType =
            allWorkouts.size - workoutsAfterTypeFilter.size

        val workoutsToSync = if (
            request.skipSynced &&
            workoutsAfterTypeFilter.isNotEmpty()
        ) {
            val plannedWorkouts =
                targetWorkoutRepository.getWorkoutsFromCalendar(
                    request.startDate,
                    request.endDate
                )

            workoutsAfterTypeFilter.filter { sourceWorkout ->
                plannedWorkouts.none { targetWorkout ->
                    hasSameExternalId(
                        sourceWorkout.details.externalData,
                        targetWorkout.details.externalData
                    )
                }
            }
        } else {
            workoutsAfterTypeFilter
        }

        val skippedAlreadySynced =
            workoutsAfterTypeFilter.size - workoutsToSync.size

        val saveResult = saveWorkoutsIndividually(
            repository = targetWorkoutRepository,
            workouts = workoutsToSync
        )

        val response = CopyWorkoutsResponse(
            copied = saveResult.copied,
            filteredOut = skippedByType + skippedAlreadySynced,
            skippedByType = skippedByType,
            skippedAlreadySynced = skippedAlreadySynced,
            startDate = request.startDate,
            endDate = request.endDate,
            externalData = ExternalData.empty(),
            failed = saveResult.failures.size,
            failedWorkouts = saveResult.failures
        )

        log.info(
            "Calendar sync completed. copied={}, skippedByType={}, " +
                "skippedAlreadySynced={}, failed={}",
            response.copied,
            response.skippedByType,
            response.skippedAlreadySynced,
            response.failed
        )

        return response
    }

    private fun shouldReconcileChangedWorkouts(
        request: CopyFromCalendarToCalendarRequest
    ): Boolean {
        return request.replaceChangedWorkouts &&
            request.sourcePlatform == Platform.TRAINER_ROAD &&
            request.targetPlatform == Platform.TRAINING_PEAKS
    }
    
    fun copyWorkoutsC2L(request: CopyFromCalendarToLibraryRequest): CopyWorkoutsResponse {
        log.info("Received request for copy calendar to library: $request")
        val sourceWorkoutRepository = workoutRepositoryMap[request.sourcePlatform]!!
        val targetWorkoutRepository = workoutRepositoryMap[request.targetPlatform]!!
        val targetPlanRepository = planRepositoryMap[request.targetPlatform]!!

        val allWorkouts = sourceWorkoutRepository.getWorkoutsFromCalendar(request.startDate, request.endDate)
        val filteredWorkouts = allWorkouts.filter { request.types.contains(it.details.type) }

        val newPlan = targetPlanRepository.createLibraryContainer(request.name, request.isPlan, request.startDate)
        targetWorkoutRepository.saveWorkoutsToLibrary(newPlan, filteredWorkouts)
        val skippedByType = allWorkouts.size - filteredWorkouts.size

        return CopyWorkoutsResponse(
            copied = filteredWorkouts.size,
            filteredOut = skippedByType,
            skippedByType = skippedByType,
            skippedAlreadySynced = 0,
            startDate = request.startDate,
            endDate = request.endDate,
            externalData = newPlan.externalData
        )
    }

    fun copyWorkoutL2L(request: CopyFromLibraryToLibraryRequest): CopyWorkoutsResponse {
        log.info("Received request for copy library to library: $request")
        val sourceWorkoutRepository = workoutRepositoryMap[request.sourcePlatform]!!
        val targetWorkoutRepository = workoutRepositoryMap[request.targetPlatform]!!

        val workout = sourceWorkoutRepository.getWorkoutFromLibrary(request.workoutExternalData)
        targetWorkoutRepository.saveWorkoutsToLibrary(request.targetLibraryContainer, listOf(workout))
        return CopyWorkoutsResponse(
            copied = 1,
            filteredOut = 0,
            skippedByType = 0,
            skippedAlreadySynced = 0,
            startDate = LocalDate.now(),
            endDate = LocalDate.now(),
            externalData = request.targetLibraryContainer.externalData
        )
    }

    fun findWorkoutsByName(platform: Platform, name: String): List<WorkoutDetails> {
        log.info("Received request for find workouts by name, platform: $platform, name: $name")
        return workoutRepositoryMap[platform]!!.findWorkoutsFromLibraryByName(name)
    }

    fun deleteWorkoutsFromCalendar(request: DeleteWorkoutRequestDTO) {
        log.info("Received request to delete workouts from calendar: $request")
        val workoutRepository = workoutRepositoryMap[request.platform]!!
        workoutRepository.deleteWorkoutsFromCalendar(request.startDate, request.endDate)
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
