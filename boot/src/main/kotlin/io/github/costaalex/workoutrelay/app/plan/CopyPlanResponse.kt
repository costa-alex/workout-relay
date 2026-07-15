package io.github.costaalex.workoutrelay.app.plan

import io.github.costaalex.workoutrelay.domain.ExternalData

data class CopyPlanResponse(
    val planName: String,
    val workouts: Int,
    val externalData: ExternalData,
)
