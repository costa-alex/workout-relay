package io.github.costaalex.workoutrelay.domain.workout

import io.github.costaalex.workoutrelay.domain.ExternalData
import io.github.costaalex.workoutrelay.domain.TrainingType
import io.github.costaalex.workoutrelay.domain.workout.structure.WorkoutStructure
import java.io.Serializable
import java.time.LocalDate

data class Workout(
    val details: WorkoutDetails,
    val date: LocalDate?,
    val structure: WorkoutStructure?,
) : Serializable {
    companion object {
        fun note(date: LocalDate, name: String, description: String?, externalData: ExternalData): Workout {
            return Workout(WorkoutDetails(TrainingType.NOTE, TrainingType.UNKNOWN, name, description, null, null, externalData), date, null)
        }
    }

    fun withDate(date: LocalDate): Workout {
        return Workout(details, date, structure)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Workout

        return details == other.details
    }

    override fun hashCode(): Int {
        return details.hashCode()
    }


}
