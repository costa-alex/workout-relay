package io.github.costaalex.workoutrelay.infrastructure.platform.trainerroad.workout

import io.github.costaalex.workoutrelay.domain.ExternalData
import io.github.costaalex.workoutrelay.domain.TrainingType
import io.github.costaalex.workoutrelay.domain.workout.Workout
import io.github.costaalex.workoutrelay.domain.workout.WorkoutDetails
import io.github.costaalex.workoutrelay.domain.workout.structure.SingleStep
import io.github.costaalex.workoutrelay.domain.workout.structure.StepIntensity
import io.github.costaalex.workoutrelay.domain.workout.structure.StepLength
import io.github.costaalex.workoutrelay.domain.workout.structure.StepTarget
import io.github.costaalex.workoutrelay.domain.workout.structure.WorkoutStep
import io.github.costaalex.workoutrelay.domain.workout.structure.WorkoutStructure
import org.slf4j.LoggerFactory
import java.time.Duration
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class TrainerRoadWorkoutMapper {
    private val log = LoggerFactory.getLogger(this.javaClass)
    
    fun toWorkout(trWorkoutResponseDTO: TRWorkoutResponseDTO, removeHtmlTags: Boolean): Workout {
        val trWorkout: TRWorkoutResponseDTO.TRWorkout = trWorkoutResponseDTO.workout
        val steps = convertSteps(
            intervals = trWorkout.intervalData,
            workoutData = trWorkout.workoutData
        )

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

    private fun convertSteps(
        intervals: List<
            TRWorkoutResponseDTO.IntervalsDataDTO
        >,
        workoutData: List<
            TRWorkoutResponseDTO.WorkoutDataPointDTO
        >
    ): List<WorkoutStep> {

        val ftpPercentByTick: Map<Int, Double> =
            workoutData
                .mapNotNull { point ->
                    point.ftpPercent?.let { ftpPercent ->
                        point.tick to ftpPercent
                    }
                }
                .toMap()

        val relevantIntervals =
            intervals.filterNot { interval ->
                interval.name == "Workout"
            }

        if (relevantIntervals.isEmpty()) {
            return convertWorkoutDataToSteps(workoutData)
        }

        val lastIntervalIndex =
            relevantIntervals.lastIndex

        return relevantIntervals.mapIndexed {
            index,
            interval ->

            mapInterval(
                interval = interval,
                ftpPercentByTick =
                    ftpPercentByTick,
                isFirstInterval =
                    index == 0,
                isLastInterval =
                    index == lastIntervalIndex
            )
        }
    }

    private fun convertWorkoutDataToSteps(
        workoutData: List<TRWorkoutResponseDTO.WorkoutDataPointDTO>
    ): List<WorkoutStep> {
        val points = workoutData
            .mapNotNull { point ->
                point.ftpPercent?.let { PowerPoint(point.tick, it) }
            }
            .sortedBy { it.tick }

        if (points.size < 2) {
            return emptyList()
        }

        val boundaryIndexes = mutableListOf(0)
        var previousSlope = slope(points[0], points[1])

        for (index in 2 until points.size) {
            val currentSlope = slope(points[index - 1], points[index])
            if (abs(currentSlope - previousSlope) > SLOPE_TOLERANCE) {
                boundaryIndexes += index - 1
                previousSlope = currentSlope
            }
        }

        if (boundaryIndexes.last() != points.lastIndex) {
            boundaryIndexes += points.lastIndex
        }

        val segmentCount = boundaryIndexes.size - 1
        return boundaryIndexes.zipWithNext().mapIndexedNotNull { index, (startIndex, endIndex) ->
            val start = points[startIndex]
            val end = points[endIndex]
            val duration = end.tick - start.tick
            if (duration <= 0) {
                return@mapIndexedNotNull null
            }

            val targetStart = start.ftpPercent.roundToInt()
            val targetEnd = end.ftpPercent.roundToInt()
            SingleStep(
                name = when {
                    segmentCount == 1 -> "Step"
                    index == 0 -> "Warm Up"
                    index == segmentCount - 1 -> "Cool Down"
                    else -> "Step"
                },
                length = StepLength.seconds(duration.toLong()),
                target = StepTarget(targetStart, targetEnd),
                cadence = null,
                ramp = targetStart != targetEnd,
                intensity = when {
                    segmentCount == 1 -> StepIntensity.ACTIVE
                    index == 0 -> StepIntensity.WARM_UP
                    index == segmentCount - 1 -> StepIntensity.COOL_DOWN
                    else -> StepIntensity.ACTIVE
                },
            )
        }
    }

    private fun slope(start: PowerPoint, end: PowerPoint): Double =
        (end.ftpPercent - start.ftpPercent) / (end.tick - start.tick)

    private data class PowerPoint(
        val tick: Int,
        val ftpPercent: Double,
    )

    private companion object {
        const val SLOPE_TOLERANCE = 0.0001
    }

    private fun getDescription(description: String, removeHtmlTags: Boolean): String =
        if (removeHtmlTags) {
            description.replace("<[^>]*>".toRegex(), " ").replace("&" + "nbsp;", " ").replace("\\s+".toRegex(), " ")
        } else {
            description
        }.trim()

    private fun mapInterval(
        interval:
            TRWorkoutResponseDTO.IntervalsDataDTO,
        ftpPercentByTick: Map<Int, Double>,
        isFirstInterval: Boolean,
        isLastInterval: Boolean
    ): SingleStep {

        val startTick = interval.start.roundToInt()

        val endExclusiveTick = interval.end.roundToInt()

        val lastIntervalTick =
            (endExclusiveTick - 1)
                .coerceAtLeast(startTick)

        val fallbackTarget = interval.targetStart()

        val targetStart =
            ftpPercentByTick[startTick]
                ?.roundToInt()
                ?: fallbackTarget

        val targetEnd =
            ftpPercentByTick[lastIntervalTick]
                ?.roundToInt()
                ?: targetStart

        val isRamp = targetStart != targetEnd

        val intensity =
            if (
                interval.isFake &&
                !isFirstInterval &&
                !isLastInterval
            ) {
                StepIntensity.RECOVERY
            } else {
                null
            }

        log.debug(
            "Mapped TrainerRoad interval. name={}, startTick={}, lastTick={}, targetStart={}, targetEnd={}, ramp={}, intensity={}",
            interval.name,
            startTick,
            lastIntervalTick,
            targetStart,
            targetEnd,
            isRamp,
            intensity
        )

        return SingleStep(
            name =
                if (interval.name == "Fake") {
                    "Step"
                } else {
                    interval.name
                },
            length = StepLength.seconds(
                (
                    interval.end -
                        interval.start
                ).roundToLong()
            ),
            target = StepTarget(
                start = targetStart,
                end = targetEnd
            ),
            cadence = null,
            ramp = isRamp,
            intensity = intensity
        )
    }
}
