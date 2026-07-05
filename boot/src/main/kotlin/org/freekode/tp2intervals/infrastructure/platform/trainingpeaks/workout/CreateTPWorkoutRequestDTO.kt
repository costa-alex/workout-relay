package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout

import java.time.LocalDate
import org.freekode.tp2intervals.domain.activity.Activity
import org.freekode.tp2intervals.domain.workout.Workout

class CreateTPWorkoutRequestDTO(
    var athleteId: String,
    var workoutDay: LocalDate,
    var workoutTypeValueId: Int,
    var workoutSubTypeValueId: Int?,
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
                athleteId,
                workout.date ?: LocalDate.now(),
                TPTrainingTypeMapper.getByType(workout.details.type),
                49, // TODO: Map workout subtype to TrainingPeaks subtype value ID
                buildDescription(workout),
                null,
                workout.details.duration?.toMinutes()?.toDouble()?.div(60),
                null,
                workout.details.load,
                structureStr
            )
        }

        fun createActivity(athleteId: String, activity: Activity): CreateTPWorkoutRequestDTO {
            TODO("Not yet implemented")
        }

    }
}
