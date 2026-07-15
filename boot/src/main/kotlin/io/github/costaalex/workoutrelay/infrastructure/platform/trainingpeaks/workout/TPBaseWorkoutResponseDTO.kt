package io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.workout

import io.github.costaalex.workoutrelay.domain.TrainingType
import io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.workout.structure.TPWorkoutStructureDTO

abstract class TPBaseWorkoutResponseDTO(
    val id: String,
    val workoutTypeValueId: Int?,
    val workoutSubTypeValueId: Int?,
    val title: String?,
    val totalTimePlanned: Double?,
    val tssPlanned: Int?,
    val description: String?,
    val coachComments: String?,
    val structure: TPWorkoutStructureDTO?
) {
    fun getWorkoutType(): TrainingType? = workoutTypeValueId?.let { TPTrainingTypeMapper.getByValue(it) }
    fun getWorkoutSubType(): TrainingType? = workoutSubTypeValueId?.let { TPTrainingTypeMapper.getSubtypeByValue(it) }
}
