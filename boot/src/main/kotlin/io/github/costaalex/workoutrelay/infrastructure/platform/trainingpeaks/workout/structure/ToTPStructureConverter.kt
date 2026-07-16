package io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.workout.structure

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.costaalex.workoutrelay.domain.workout.structure.MultiStep
import io.github.costaalex.workoutrelay.domain.workout.structure.SingleStep
import io.github.costaalex.workoutrelay.domain.workout.structure.StepLength
import io.github.costaalex.workoutrelay.domain.workout.structure.WorkoutStep
import io.github.costaalex.workoutrelay.domain.workout.structure.StepTarget
import io.github.costaalex.workoutrelay.domain.workout.structure.WorkoutStructure
import io.github.costaalex.workoutrelay.domain.workout.structure.StepIntensity
import kotlin.math.ceil
import kotlin.math.roundToInt
import org.slf4j.LoggerFactory

class ToTPStructureConverter(
    private val objectMapper: ObjectMapper,
    private val structure: WorkoutStructure,
) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    companion object {
        fun toStructureString(objectMapper: ObjectMapper, structure: WorkoutStructure) =
            ToTPStructureConverter(objectMapper, structure).toTPStructureStr()
    }

    fun toTPStructureStr(): String {
        val mappedStructure =
            mapToWorkoutStructure(
                structure.steps
            )

        val json =
            objectMapper.copy()
                .setSerializationInclusion(
                    JsonInclude.Include.NON_NULL
                )
                .writeValueAsString(
                    mappedStructure
                )

        log.info(
            "TrainingPeaks workout structure={}",
            json
        )

        return json
    }

    private fun toIntensityClass(
        step: SingleStep
    ): TPIntensityClass {

        when (step.intensity) {
            StepIntensity.WARM_UP ->
                return TPIntensityClass.WARM_UP

            StepIntensity.ACTIVE ->
                return TPIntensityClass.ACTIVE

            StepIntensity.RECOVERY ->
                return TPIntensityClass.REST

            StepIntensity.COOL_DOWN ->
                return TPIntensityClass.COOL_DOWN

            null -> Unit
        }

        val normalizedName =
            step.name
                .orEmpty()
                .lowercase()
                .replace(
                    Regex("[\\s_-]"),
                    ""
                )

        return when {
            normalizedName.contains("warmup") ->
                TPIntensityClass.WARM_UP

            normalizedName.contains("cooldown") ->
                TPIntensityClass.COOL_DOWN

            normalizedName.contains("recover") ||
                normalizedName.contains("recovery") ||
                normalizedName.contains("rest") ||
                normalizedName.contains("easy") ->
                TPIntensityClass.REST

            else ->
                TPIntensityClass.ACTIVE
        }
    }
    
    private fun mapToWorkoutStructure(
        steps: List<WorkoutStep>
    ): TPWorkoutStructureDTO {

        val lastStepIndex =
            steps.lastIndex

        val stepDTOs =
            steps.mapIndexed {
                index,
                workoutStep ->

                mapToStructureStep(
                    workoutStep = workoutStep,
                    isFirstStep =
                        index == 0,
                    isLastStep =
                        index == lastStepIndex
                )
            }

        val distanceBased =
            isDistanceBased(steps)

        return TPWorkoutStructureDTO(
            stepDTOs,
            if (distanceBased) {
                "distance"
            } else {
                "duration"
            },
            TPTargetMapper.getByTargetUnit(
                structure.target
            ),
            if (distanceBased) {
                "meter"
            } else {
                null
            }
        )
    }

    private fun isDistanceBased(steps: List<WorkoutStep>): Boolean {
        for (step in steps) {
            if (!step.isSingleStep()) {
                val distanceBased = isDistanceBased((step as MultiStep).steps)
                if (distanceBased) {
                    return true
                }
                continue
            }
            if ((step as SingleStep).length.unit == StepLength.LengthUnit.METERS) {
                return true
            }
        }
        return false
    }

    private fun mapToStructureStep(
        workoutStep: WorkoutStep,
        isFirstStep: Boolean,
        isLastStep: Boolean
    ): TPStructureStepDTO {

        if (!workoutStep.isSingleStep()) {
            return mapMultiStep(
                workoutStep as MultiStep
            )
        }

        val singleStep =
            workoutStep as SingleStep

        return if (singleStep.ramp) {
            mapRampStep(
                singleStep = singleStep,
                isFirstStep = isFirstStep,
                isLastStep = isLastStep
            )
        } else {
            mapSingleStep(singleStep)
        }
    }

    private fun mapSingleStep(
        singleStep: SingleStep
    ): TPStructureStepDTO {

        return TPStructureStepDTO.singleStep(
            mapToStepDTO(singleStep)
        )
    }

    private fun mapMultiStep(
        workoutStep: MultiStep
    ): TPStructureStepDTO {

        val stepDTOs =
            workoutStep.steps.map {
                mapToStepDTO(it)
            }

        return TPStructureStepDTO.multiStep(
            workoutStep.repetitions,
            stepDTOs
        )
    }
    
    private fun mapToStepDTO(
        workoutStep: SingleStep,
        intensityClassOverride:
            TPIntensityClass? = null,
        nameOverride: String? = null,
        mainTargetOverride:
            TPTargetDTO? = null
    ): TPStepDTO {

        val mainTarget =
            mainTargetOverride
                ?: toMainTarget(
                    workoutStep.target
                )

        val cadenceTarget =
            workoutStep.cadence?.let {
                TPTargetDTO.cadenceTarget(
                    it.start,
                    it.end
                )
            }

        val targetList =
            listOfNotNull(
                mainTarget,
                cadenceTarget
            )

        val intensityClass =
            intensityClassOverride
                ?: toIntensityClass(
                    workoutStep
                )

        return TPStepDTO(
            name =
                nameOverride
                    ?: workoutStep.name,
            length =
                TPLengthDTO.fromStepLength(
                    workoutStep.length
                ),
            targets = targetList,
            intensityClass =
                intensityClass.apiValue
        )
    }

    private fun toMainTarget(target: StepTarget) =
        if (target.isSingleValue()) {
            TPTargetDTO.mainTarget(target.start)
        } else {
            TPTargetDTO.mainTarget(target.start, target.end)
        }

    private fun mapRampStep(
        singleStep: SingleStep,
        isFirstStep: Boolean,
        isLastStep: Boolean
    ): TPStructureStepDTO {

        val rampUp =
            singleStep.target.end >
                singleStep.target.start

        val rampDown =
            singleStep.target.end <
                singleStep.target.start

        require(rampUp || rampDown) {
            "Ramp step must have different " +
                "start and end targets"
        }

        val intensityClass =
            when {
                isFirstStep && rampUp ->
                    TPIntensityClass.WARM_UP

                isLastStep && rampDown ->
                    TPIntensityClass.COOL_DOWN

                else ->
                    toIntensityClass(
                        singleStep
                    )
            }

        val rampStepDTOs =
            toRampStepDTOs(
                singleStep = singleStep,
                intensityClass =
                    intensityClass
            )

        return TPStructureStepDTO.rampStep(
            stepDTOs = rampStepDTOs,
            rampUp = rampUp
        )
    }

    private fun toRampStepDTOs(
        singleStep: SingleStep,
        intensityClass: TPIntensityClass
    ): List<TPStepDTO> {

        require(
            singleStep.length.unit ==
                StepLength.LengthUnit.SECONDS
        ) {
            "TrainingPeaks ramps must be time based"
        }

        val totalSeconds =
            singleStep.length.value

        require(totalSeconds > 0) {
            "Ramp duration must be greater than zero"
        }

        /*
        * O TrainingPeaks utiliza ramps stepwise.
        *
        * - ramps até 1 minuto: pelo menos 2 passos;
        * - ramps superiores: aproximadamente 1 passo/minuto;
        * - nunca criamos mais passos do que segundos.
        */
        val desiredStepCount =
            maxOf(
                2,
                ceil(
                    totalSeconds.toDouble() / 60.0
                ).toInt()
            )

        val stepCount =
            minOf(
                desiredStepCount,
                totalSeconds.toInt()
            )

        val baseDuration =
            totalSeconds / stepCount

        val remainingSeconds =
            totalSeconds % stepCount

        val targetDifference =
            singleStep.target.end -
                singleStep.target.start

        return (0 until stepCount).map { index ->

            val duration =
                baseDuration +
                    if (
                        index <
                        remainingSeconds
                    ) {
                        1
                    } else {
                        0
                    }

            val ratio =
                if (stepCount == 1) {
                    0.0
                } else {
                    index.toDouble() /
                        (stepCount - 1)
                }

            val target =
                (
                    singleStep.target.start +
                        targetDifference * ratio
                ).roundToInt()

            TPStepDTO(
                name = "Ramp",
                length =
                    TPLengthDTO.fromStepLength(
                        StepLength.seconds(
                            duration
                        )
                    ),
                targets = listOf(
                    TPTargetDTO.mainTarget(
                        target
                    )
                ),
                intensityClass =
                    intensityClass.apiValue
            )
        }
    }
}
