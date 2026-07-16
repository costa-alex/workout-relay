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

class ToTPStructureConverter(
    private val objectMapper: ObjectMapper,
    private val structure: WorkoutStructure,
) {
    companion object {
        fun toStructureString(objectMapper: ObjectMapper, structure: WorkoutStructure) =
            ToTPStructureConverter(objectMapper, structure).toTPStructureStr()
    }

    fun toTPStructureStr(): String {
        val structure = mapToWorkoutStructure(structure.steps)
        return objectMapper.copy()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writeValueAsString(structure)
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
            "Ramp step must have different start and end targets"
        }

        val intensityClass =
            when {
                isFirstStep && rampUp ->
                    TPIntensityClass.WARM_UP

                isLastStep && rampDown ->
                    TPIntensityClass.COOL_DOWN

                else ->
                    toIntensityClass(singleStep)
            }

        /*
        * No target do TrainingPeaks, minValue e maxValue
        * devem continuar ordenados. A direção é indicada
        * por rampUp ou rampDown.
        */
        val minimumTarget =
            minOf(
                singleStep.target.start,
                singleStep.target.end
            )

        val maximumTarget =
            maxOf(
                singleStep.target.start,
                singleStep.target.end
            )

        val stepDTO =
            mapToStepDTO(
                workoutStep = singleStep,
                intensityClassOverride =
                    intensityClass,
                nameOverride = "Ramp",
                mainTargetOverride =
                    TPTargetDTO.mainTarget(
                        minimumTarget,
                        maximumTarget
                    )
            )

        return TPStructureStepDTO.rampStep(
            stepDTO = stepDTO,
            rampUp = rampUp
        )
    }
}
