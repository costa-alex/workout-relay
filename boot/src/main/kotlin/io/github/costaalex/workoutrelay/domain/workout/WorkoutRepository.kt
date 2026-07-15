package io.github.costaalex.workoutrelay.domain.workout

import java.time.LocalDate
import io.github.costaalex.workoutrelay.domain.ExternalData
import io.github.costaalex.workoutrelay.domain.Platform
import io.github.costaalex.workoutrelay.domain.librarycontainer.LibraryContainer

interface WorkoutRepository {

    fun platform(): Platform

    fun getWorkoutsFromCalendar(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Workout>

    fun getWorkoutsFromLibrary(
        libraryContainer: LibraryContainer
    ): List<Workout>

    fun getWorkoutFromLibrary(
        externalData: ExternalData
    ): Workout

    fun findWorkoutsFromLibraryByName(
        name: String
    ): List<WorkoutDetails>

    fun saveWorkoutToCalendar(workout: Workout) {
        saveWorkoutsToCalendar(listOf(workout))
    }

    fun saveWorkoutsToCalendar(workouts: List<Workout>)

    fun saveWorkoutsToLibrary(
        libraryContainer: LibraryContainer,
        workouts: List<Workout>
    )

    fun deleteWorkoutsFromCalendar(
        startDate: LocalDate,
        endDate: LocalDate
    )
    
    fun deleteWorkoutFromCalendar(workout: Workout) {
    throw UnsupportedOperationException(
        "${platform().title} does not support individual workout deletion"
    )
}
}
