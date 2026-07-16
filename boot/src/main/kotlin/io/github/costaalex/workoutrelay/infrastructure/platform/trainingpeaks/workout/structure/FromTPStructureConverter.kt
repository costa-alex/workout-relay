package io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.workout.structure

import io.github.costaalex.workoutrelay.domain.workout.structure.*

class FromTPStructureConverter(
    private val structureDTO: TPWorkoutStructureDTO
) {
    companion object {
        fun toWorkoutStructure(structure: TPWorkoutStructureDTO): WorkoutStructure {
            val steps = FromTPStructureConverter(structure).mapToWorkoutSteps()
            return WorkoutStructure(structure.toTargetUnit(), steps)
        }
    }

    fun mapToWorkoutSteps(): List<WorkoutStep> {
        return structureDTO.structure.map {
            when (it.type) {
                "step" -> mapSingleStep(it.steps.firstOrNull() ?: throw IllegalArgumentException("There is no step"))
                "repetition" -> mapMultiStep(it)
                "rampUp" -> mapRampStep(it)
                "rampDown" -> mapRampStep(it)
                else -> throw IllegalArgumentException("Unknown step type: ${it.type}")
            }
        }
    }

    private fun mapSingleStep(
        tpStepDTO: TPStepDTO
    ): SingleStep {

        return SingleStep(
            name = tpStepDTO.name,
            length =
                requireNotNull(
                    tpStepDTO.length
                ).toStepLength(),
            target =
                tpStepDTO.toMainTarget(),
            cadence =
                tpStepDTO.toSecondaryTarget(),
            ramp = false,
            intensity =
                toStepIntensity(
                    tpStepDTO.intensityClass
                )
        )
    }

    private fun mapMultiStep(tPStructureStepDTO: TPStructureStepDTO): WorkoutStep {
        return MultiStep(
            null,
            tPStructureStepDTO.length!!.reps().toInt(),
            tPStructureStepDTO.steps.map { mapSingleStep(it) },
        )
    }

    private fun mapRampStep(
        structureStep:
            TPStructureStepDTO
    ): SingleStep {

        val tpStep =
            structureStep.steps
                .firstOrNull()
                ?: throw IllegalArgumentException(
                    "Ramp does not contain a step"
                )

        val mappedStep =
            mapSingleStep(tpStep)

        val minimumTarget =
            minOf(
                mappedStep.target.start,
                mappedStep.target.end
            )

        val maximumTarget =
            maxOf(
                mappedStep.target.start,
                mappedStep.target.end
            )

        val rampTarget =
            when (structureStep.type) {
                "rampUp" ->
                    StepTarget(
                        minimumTarget,
                        maximumTarget
                    )

                "rampDown" ->
                    StepTarget(
                        maximumTarget,
                        minimumTarget
                    )

                else ->
                    throw IllegalArgumentException(
                        "Unsupported ramp type: " +
                            structureStep.type
                    )
            }

        return SingleStep(
            name = mappedStep.name,
            length = mappedStep.length,
            target = rampTarget,
            cadence = mappedStep.cadence,
            ramp = true,
            intensity = mappedStep.intensity
        )
    }

    private fun toStepIntensity(
        intensityClass: String?
    ): StepIntensity? {

        return when (intensityClass) {
            TPIntensityClass.WARM_UP.apiValue ->
                StepIntensity.WARM_UP

            TPIntensityClass.ACTIVE.apiValue ->
                StepIntensity.ACTIVE

            TPIntensityClass.REST.apiValue ->
                StepIntensity.RECOVERY

            TPIntensityClass.COOL_DOWN.apiValue ->
                StepIntensity.COOL_DOWN

            else ->
                null
        }
    }
}
