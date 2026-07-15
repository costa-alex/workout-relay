package io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.workout

import java.time.LocalDate
import io.github.costaalex.workoutrelay.domain.activity.Activity
import io.github.costaalex.workoutrelay.domain.workout.Workout

class CreateTPWorkoutRequestDTO(
    var athleteId: String,
    var workoutDay: LocalDate,
    var workoutTypeValueId: Int,
    var workoutSubTypeId: Int,
    var title: String,
    var description: String?,
    var totalTime: Double?,
    var totalTimePlanned: Double?,
    var tssActual: Int?,
    var tssPlanned: Int?,
    var structure: String?
) {

    companion object {

        private fun buildDescription(workout: Workout): String {
            return listOfNotNull(
                workout.details.description?.takeIf { it.isNotBlank() },
                workout.details.externalData.toSimpleString()
            ).joinToString("\n\n")
        }
        
        fun planWorkout(
            athleteId: String, workout: Workout, structureStr: String?
        ): CreateTPWorkoutRequestDTO {
            return CreateTPWorkoutRequestDTO(
                athleteId = athleteId,
                workoutDay = workout.date ?: LocalDate.now(),
                workoutTypeValueId = TPTrainingTypeMapper.getWorkoutTypeValueId(workout.details.type),
                workoutSubTypeId = TPTrainingTypeMapper.getWorkoutSubTypeValueId(workout.details.subType),
                title = workout.details.name,
                description = buildDescription(workout),
                totalTime = null,
                totalTimePlanned = workout.details.duration?.toMinutes()?.toDouble()?.div(60),
                tssActual = null,
                tssPlanned = workout.details.load,
                structure = structureStr
            )
        }

        fun createActivity(athleteId: String, activity: Activity): CreateTPWorkoutRequestDTO {
            TODO("Not yet implemented")
        }

    }
}
