package io.github.costaalex.workoutrelay.rest.workout

import io.github.costaalex.workoutrelay.domain.ExternalData

class WorkoutDetailsDTO(
    val name: String,
    val duration: String?,
    val load: Int?,
    val externalData: ExternalData,
)
