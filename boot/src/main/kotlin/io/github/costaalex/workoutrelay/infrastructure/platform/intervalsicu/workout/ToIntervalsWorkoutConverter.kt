package io.github.costaalex.workoutrelay.infrastructure.platform.intervalsicu.workout

import io.github.costaalex.workoutrelay.domain.librarycontainer.LibraryContainer
import io.github.costaalex.workoutrelay.domain.workout.Workout
import io.github.costaalex.workoutrelay.infrastructure.Signature
import io.github.costaalex.workoutrelay.infrastructure.utils.Date
import java.time.LocalDate

class ToIntervalsWorkoutConverter {
    private val unwantedStepRegex = "^[-*]".toRegex(RegexOption.MULTILINE)

    fun createWorkoutRequestDTO(libraryContainer: LibraryContainer, workout: Workout): CreateWorkoutRequestDTO {
        val workoutString = getWorkoutString(workout)
        var description = getDescription(workout, workoutString)
        val name: String
        if (workout.details.name.length > 80) {
            name = workout.details.name.substring(0, 76).trim() + "..."
            description = "Name: ${workout.details.name}\n" + description
        } else {
            name = workout.details.name
        }
        val request = CreateWorkoutRequestDTO(
            libraryContainer.externalData.intervalsId.toString(),
            Date.daysDiff(libraryContainer.startDate, workout.date ?: LocalDate.now()),
            IntervalsTrainingTypeMapper.getByTrainingType(workout.details.type),
            name,
            workout.details.duration?.seconds,
            workout.details.load,
            description,
            null,
        )
        return request
    }

    fun createEventRequestDTO(workout: Workout): CreateEventRequestDTO {
        val workoutString = getWorkoutString(workout)
        val description = getDescription(workout, workoutString)
        return CreateEventRequestDTO(
            (workout.date ?: LocalDate.now()).atStartOfDay().toString(),
            workout.details.name,
            IntervalsTrainingTypeMapper.getByTrainingType(workout.details.type),
            "WORKOUT",
            description
        )
    }


    private fun getDescription(workout: Workout, workoutString: String?): String {
        var description = workout.details.description
            .orEmpty()
            .replace(unwantedStepRegex, "`-")
            .let { "$it\n- - - -\n${Signature.description}" }
        description += workoutString
            ?.let { "\n\n- - - -\n$it" }
            .orEmpty()
        description += "\n\n${workout.details.externalData.toSimpleString()}"
        return description
    }

    private fun getWorkoutString(workout: Workout) =
        if (workout.structure != null) {
            ToIntervalsStructureConverter(workout.structure).toIntervalsStructureStr()
        } else {
            null
        }
}
