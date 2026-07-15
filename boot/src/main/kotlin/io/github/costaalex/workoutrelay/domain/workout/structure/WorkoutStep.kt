package io.github.costaalex.workoutrelay.domain.workout.structure

import java.io.Serializable

interface WorkoutStep : Serializable {
    fun isSingleStep(): Boolean
}
