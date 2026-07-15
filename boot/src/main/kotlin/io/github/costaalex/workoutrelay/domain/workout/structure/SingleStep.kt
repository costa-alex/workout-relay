package io.github.costaalex.workoutrelay.domain.workout.structure

import io.github.costaalex.workoutrelay.utils.RampConverter

class SingleStep(
    val name: String?,
    val length: StepLength,
    val target: StepTarget,
    val cadence: StepTarget?,
    val ramp: Boolean
) : WorkoutStep {
    override fun isSingleStep() = true

    fun convertRampToMultiStep(): MultiStep {
        if (!ramp) {
            throw IllegalStateException("Step is not ramp step")
        }
        return RampConverter(this).toRampToMultiStep()
    }
}
