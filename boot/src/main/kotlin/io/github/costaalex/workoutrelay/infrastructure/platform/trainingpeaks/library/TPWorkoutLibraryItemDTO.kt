package io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.library

import io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.workout.TPBaseWorkoutResponseDTO
import io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.workout.structure.TPWorkoutStructureDTO

class TPWorkoutLibraryItemDTO(
    exerciseLibraryItemId: String,
    workoutTypeId: Int,
    workoutSubTypeId: Int,
    itemName: String,
    totalTimePlanned: Double?,
    tssPlanned: Int?,
    description: String?,
    coachComments: String?,
    structure: TPWorkoutStructureDTO?
) : TPBaseWorkoutResponseDTO(
    exerciseLibraryItemId,
    workoutTypeId,
    workoutSubTypeId,
    itemName,
    totalTimePlanned,
    tssPlanned,
    description,
    coachComments,
    structure
)
