package io.github.costaalex.workoutrelay.domain.workout.structure

class MultiStep(
    val name: String?,
    val repetitions: Int,
    val steps: List<SingleStep>
) : WorkoutStep {
    override fun isSingleStep() = false
}
