package io.github.costaalex.workoutrelay.infrastructure.platform.intervalsicu.workout

import io.github.costaalex.workoutrelay.domain.TrainingType

class IntervalsTrainingTypeMapper {
    companion object {
        private val typeMap = mapOf(
            TrainingType.BIKE to "Ride",
            TrainingType.MTB to "MountainBikeRide",
            TrainingType.VIRTUAL_BIKE to "VirtualRide",
            TrainingType.RUN to "Run",
            TrainingType.SWIM to "Swim",
            TrainingType.WEIGHT to "WeightTraining",
            TrainingType.NOTE to "NOTE",
            TrainingType.UNKNOWN to "Other",
            TrainingType.WALK to "Walk",
        )

        fun getByIntervalsType(intervalsType: String): TrainingType =
            typeMap.filterValues { it == intervalsType }.keys.firstOrNull() ?: TrainingType.UNKNOWN

        fun getByTrainingType(trainingType: TrainingType): String = typeMap[trainingType]!!
    }
}
