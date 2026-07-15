package io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.workout

import io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.workout.structure.TPWorkoutStructureDTO
import java.time.LocalDateTime

class TPWorkoutCalendarResponseDTO(
    val workoutDay: LocalDateTime,
    workoutId: String,
    workoutTypeValueId: Int?,
    workoutSubTypeValueId: Int?,
    title: String?,
    totalTimePlanned: Double?,
    tssPlanned: Int?,
    description: String?,
    coachComments: String?,
    structure: TPWorkoutStructureDTO?
): TPBaseWorkoutResponseDTO(
    workoutId,
    workoutTypeValueId,
    workoutSubTypeValueId,
    title,
    totalTimePlanned,
    tssPlanned,
    description,
    coachComments,
    structure
)
