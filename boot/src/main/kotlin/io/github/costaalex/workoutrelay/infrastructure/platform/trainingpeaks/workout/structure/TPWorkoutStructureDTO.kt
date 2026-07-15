package io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.workout.structure

import io.github.costaalex.workoutrelay.domain.workout.structure.WorkoutStructure

class TPWorkoutStructureDTO(
    val structure: List<TPStructureStepDTO>,
    val primaryLengthMetric: String?, // distance, duration
    val primaryIntensityMetric: String?,
    val visualizationDistanceUnit: String?, // meter, ?
) {
    fun toTargetUnit(): WorkoutStructure.TargetUnit = TPTargetMapper.getByIntensity(primaryIntensityMetric!!)
}
