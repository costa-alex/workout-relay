package io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.workout.structure

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.costaalex.workoutrelay.domain.workout.structure.MultiStep
import io.github.costaalex.workoutrelay.domain.workout.structure.SingleStep
import io.github.costaalex.workoutrelay.domain.workout.structure.StepLength
import io.github.costaalex.workoutrelay.domain.workout.structure.WorkoutStep
import io.github.costaalex.workoutrelay.domain.workout.structure.StepTarget
import io.github.costaalex.workoutrelay.domain.workout.structure.WorkoutStructure

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

    private fun toIntensityClass(step: SingleStep): TPIntensityClass {
        val normalizedName = step.name
            .orEmpty()
            .lowercase()
            .replace(Regex("[\\s_-]"), "")

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
    
    private fun mapToWorkoutStructure(steps: List<WorkoutStep>): TPWorkoutStructureDTO {
        val stepDTOs = steps.map { mapToStructureStep(it) }

        val distanceBased = isDistanceBased(steps)

        return TPWorkoutStructureDTO(
            stepDTOs,
            if (distanceBased) "distance" else "duration",
            TPTargetMapper.getByTargetUnit(structure.target),
            if (distanceBased) "meter" else null,
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

    private fun mapToStructureStep(workoutStep: WorkoutStep): TPStructureStepDTO {
        return if (workoutStep.isSingleStep()) {
            val singleStep = workoutStep as SingleStep
            if (singleStep.ramp) {
                mapMultiStep(workoutStep.convertRampToMultiStep())
            } else {
                mapSingleStep(singleStep)
            }
        } else {
            mapMultiStep(workoutStep as MultiStep)
        }
    }

    private fun mapSingleStep(singleStep: SingleStep): TPStructureStepDTO {
        val singleStepDTO = mapToStepDTO(singleStep)
        return TPStructureStepDTO.singleStep(singleStepDTO)
    }

    private fun mapMultiStep(workoutStep: MultiStep): TPStructureStepDTO {
        val stepDTOs = workoutStep.steps.map { mapToStepDTO(it) }
        return TPStructureStepDTO.multiStep(workoutStep.repetitions, stepDTOs)
    }
    
    private fun mapToStepDTO(workoutStep: SingleStep): TPStepDTO {
        val mainTarget = toMainTarget(workoutStep.target)
        val cadenceTarget = workoutStep.cadence?.let { TPTargetDTO.cadenceTarget(it.start, it.end) }
        val targetList = mutableListOf(mainTarget, cadenceTarget).filterNotNull()

        return TPStepDTO(
            workoutStep.name,
            TPLengthDTO.fromStepLength(workoutStep.length),
            targetList,
            toIntensityClass(workoutStep).apiValue
        )
    }

    private fun toMainTarget(target: StepTarget) =
        if (target.isSingleValue()) {
            TPTargetDTO.mainTarget(target.start)
        } else {
            TPTargetDTO.mainTarget(target.start, target.end)
        }
}
