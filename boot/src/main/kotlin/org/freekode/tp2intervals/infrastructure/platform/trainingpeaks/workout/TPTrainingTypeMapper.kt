package org.freekode.tp2intervals.infrastructure.platform.trainingpeaks.workout

import org.freekode.tp2intervals.domain.TrainingType

class TPTrainingTypeMapper {
    companion object {

        private val workoutTypeMap = mapOf(
            TrainingType.SWIM to 1,
            TrainingType.BIKE to 2,
            TrainingType.VIRTUAL_BIKE to 2,
            TrainingType.MTB to 2,
            TrainingType.RUN to 3,
            TrainingType.WALK to 3,
            TrainingType.WEIGHT to 9,
            TrainingType.NOTE to 7,
            TrainingType.UNKNOWN to 100
        )

        private val workoutSubTypeMap = mapOf(
            TrainingType.SWIM to 1,
            TrainingType.BIKE to 2,
            TrainingType.VIRTUAL_BIKE to 49,
            TrainingType.RUN to 3,
            TrainingType.MTB to 8,
            TrainingType.WEIGHT to 9,
            TrainingType.NOTE to 7,
            TrainingType.WALK to 13,
            TrainingType.UNKNOWN to 100
        )

        fun getWorkoutTypeValueId(trainingType: TrainingType): Int =
            workoutTypeMap[trainingType] ?: workoutTypeMap[TrainingType.UNKNOWN]!!

        fun getWorkoutSubTypeValueId(trainingType: TrainingType): Int =
            workoutSubTypeMap[trainingType] ?: workoutSubTypeMap[TrainingType.UNKNOWN]!!

        fun getByValue(value: Int): TrainingType =
            workoutSubTypeMap.filterValues { it == value }.keys.firstOrNull()
                ?: TrainingType.UNKNOWN

        /**
         * Keep this for backwards compatibility with existing code.
         * Prefer getWorkoutTypeValueId() or getWorkoutSubTypeValueId().
         */
        fun getByType(trainingType: TrainingType): Int =
            getWorkoutTypeValueId(trainingType)
    }
}