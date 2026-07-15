package io.github.costaalex.workoutrelay.infrastructure.platform.trainerroad.workout

import io.github.costaalex.workoutrelay.domain.ExternalData
import io.github.costaalex.workoutrelay.domain.TrainingType
import io.github.costaalex.workoutrelay.domain.workout.Workout
import io.github.costaalex.workoutrelay.domain.workout.WorkoutDetails
import io.github.costaalex.workoutrelay.domain.workout.structure.*
import java.time.Duration
import kotlin.math.roundToInt
import org.slf4j.LoggerFactory

class TrainerRoadWorkoutMapper {
    private val log = LoggerFactory.getLogger(this.javaClass)
    
    fun toWorkout(trWorkoutResponseDTO: TRWorkoutResponseDTO, removeHtmlTags: Boolean): Workout {
        val trWorkout: TRWorkoutResponseDTO.TRWorkout = trWorkoutResponseDTO.workout
        val steps = convertSteps(trWorkout.intervalData)
        return Workout(
            toWorkoutDetails(trWorkout.details, removeHtmlTags),
            null,
            steps.takeIf { it.isNotEmpty() }
                ?.let { WorkoutStructure(WorkoutStructure.TargetUnit.FTP_PERCENTAGE, it) },
        )
    }

    fun toWorkoutDetails(detailsDTO: TrainerRoadWorkoutDetailsDTO, removeHtmlTags: Boolean): WorkoutDetails {
        
        return WorkoutDetails(
            TrainingType.BIKE,
            if (detailsDTO.isOutside) TrainingType.BIKE else TrainingType.VIRTUAL_BIKE,
            detailsDTO.workoutName,
            getDescription(detailsDTO.workoutDescription, removeHtmlTags),
            Duration.ofMinutes(detailsDTO.duration.toLong()),
            detailsDTO.tss?.roundToInt(),
            ExternalData.empty().withTrainerRoad(detailsDTO.id)
        )
    }

    private fun convertSteps(intervals: List<TRWorkoutResponseDTO.IntervalsDataDTO>): List<WorkoutStep> {
        val steps = mutableListOf<WorkoutStep>()

        for (interval in intervals) {
            if (interval.name == "Workout") {
                continue
            }
            val stepLength = StepLength.seconds((interval.end - interval.start).toLong())
            val name = if (interval.name == "Fake") "Step" else interval.name

            val singleStep =
                SingleStep(name, stepLength, StepTarget(interval.targetStart(), interval.targetEnd()), null, false)
            steps.add(singleStep)
        }
        return steps
    }

    private fun getDescription(description: String, removeHtmlTags: Boolean): String =
        if (removeHtmlTags) {
            description.replace("<[^>]*>".toRegex(), " ").replace("&" + "nbsp;", " ").replace("\\s+".toRegex(), " ")
        } else {
            description
        }.trim()
}
