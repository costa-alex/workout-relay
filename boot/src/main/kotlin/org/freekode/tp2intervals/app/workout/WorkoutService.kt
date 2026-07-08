package org.freekode.tp2intervals.app.workout

import org.freekode.tp2intervals.domain.ExternalData
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.librarycontainer.LibraryContainerRepository
import org.freekode.tp2intervals.domain.workout.WorkoutDetails
import org.freekode.tp2intervals.domain.workout.WorkoutRepository
import org.freekode.tp2intervals.rest.workout.DeleteWorkoutRequestDTO
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

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
    
    fun copyWorkoutsC2C(request: CopyFromCalendarToCalendarRequest): CopyWorkoutsResponse {
        log.info("Received request for copy calendar to calendar: $request")

        val sourceWorkoutRepository = workoutRepositoryMap[request.sourcePlatform]!!
        val targetWorkoutRepository = workoutRepositoryMap[request.targetPlatform]!!

        val allWorkoutsToSync = sourceWorkoutRepository.getWorkoutsFromCalendar(
            request.startDate,
            request.endDate
        )

        val workoutsAfterTypeFilter = allWorkoutsToSync.filter {
            request.types.contains(it.details.type)
        }

        val skippedByType = allWorkoutsToSync.size - workoutsAfterTypeFilter.size

        val workoutsToSync = if (request.skipSynced) {
            val plannedWorkouts = targetWorkoutRepository.getWorkoutsFromCalendar(
                request.startDate,
                request.endDate
            )

            val partitionedWorkouts = workoutsAfterTypeFilter.partition { sourceWorkout ->
                plannedWorkouts.any { targetWorkout ->
                    hasSameExternalId(
                        sourceWorkout.details.externalData,
                        targetWorkout.details.externalData
                    )
                }
            }

            val alreadySyncedWorkouts = partitionedWorkouts.first
            val newWorkouts = partitionedWorkouts.second

            log.info(
                "Copy calendar to calendar filtering result. total={}, skippedByType={}, skippedAlreadySynced={}, copied={}",
                allWorkoutsToSync.size,
                skippedByType,
                alreadySyncedWorkouts.size,
                newWorkouts.size
            )

            newWorkouts
        } else {
            log.info(
                "Copy calendar to calendar filtering result. total={}, skippedByType={}, skippedAlreadySynced=0, copied={}",
                allWorkoutsToSync.size,
                skippedByType,
                workoutsAfterTypeFilter.size
            )

            workoutsAfterTypeFilter
        }

        val skippedAlreadySynced = workoutsAfterTypeFilter.size - workoutsToSync.size
        val filteredOut = skippedByType + skippedAlreadySynced

        val response = CopyWorkoutsResponse(
            copied = workoutsToSync.size,
            filteredOut = filteredOut,
            skippedByType = skippedByType,
            skippedAlreadySynced = skippedAlreadySynced,
            startDate = request.startDate,
            endDate = request.endDate,
            externalData = ExternalData.empty()
        )

        targetWorkoutRepository.saveWorkoutsToCalendar(workoutsToSync)

        log.info("Saved workouts to calendar successfully: $response")

        return response
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
}
