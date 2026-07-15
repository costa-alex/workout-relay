package io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.workout

import io.github.costaalex.workoutrelay.domain.TrainingType

object TPTrainingTypeMapper {

    private val workoutTypeMap = mapOf(
        TrainingType.SWIM to TPWorkoutType.SWIM,
        TrainingType.BIKE to TPWorkoutType.BIKE,
        TrainingType.VIRTUAL_BIKE to TPWorkoutType.BIKE,
        TrainingType.MTB to TPWorkoutType.BIKE,
        TrainingType.RUN to TPWorkoutType.RUN,
        TrainingType.WALK to TPWorkoutType.RUN,
        TrainingType.WEIGHT to TPWorkoutType.WEIGHT,
        TrainingType.NOTE to TPWorkoutType.DAY_OFF,
        TrainingType.UNKNOWN to TPWorkoutType.OTHER
    )

    private val workoutSubTypeMap = mapOf(
        TrainingType.SWIM to TPWorkoutSubType.SWIM,
        TrainingType.BIKE to TPWorkoutSubType.RIDE,
        TrainingType.VIRTUAL_BIKE to TPWorkoutSubType.VIRTUAL_RIDE,
        TrainingType.MTB to TPWorkoutSubType.MOUNTAIN_BIKE,
        TrainingType.RUN to TPWorkoutSubType.RUN,
        TrainingType.WALK to TPWorkoutSubType.WALK,
        TrainingType.WEIGHT to TPWorkoutSubType.WEIGHT,
        TrainingType.NOTE to TPWorkoutSubType.DAY_OFF,
        TrainingType.UNKNOWN to TPWorkoutSubType.OTHER
    )

    fun getWorkoutType(
        trainingType: TrainingType
    ): TPWorkoutType =
        workoutTypeMap[trainingType] ?: TPWorkoutType.OTHER

    fun getWorkoutSubType(
        trainingType: TrainingType
    ): TPWorkoutSubType =
        workoutSubTypeMap[trainingType] ?: TPWorkoutSubType.OTHER

    fun getWorkoutTypeValueId(
        trainingType: TrainingType
    ): Int =
        getWorkoutType(trainingType).valueId

    fun getWorkoutSubTypeValueId(
        trainingType: TrainingType
    ): Int =
        getWorkoutSubType(trainingType).valueId

    fun getByValue(value: Int): TrainingType =
        workoutTypeMap.entries
            .firstOrNull { it.value.valueId == value }
            ?.key
            ?: TrainingType.UNKNOWN

    fun getSubtypeByValue(value: Int): TrainingType =
        workoutSubTypeMap.entries
            .firstOrNull { it.value.valueId == value }
            ?.key
            ?: TrainingType.UNKNOWN
}