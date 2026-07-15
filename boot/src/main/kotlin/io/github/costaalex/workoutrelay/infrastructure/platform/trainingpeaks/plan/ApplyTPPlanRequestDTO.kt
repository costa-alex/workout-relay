package io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.plan

class ApplyTPPlanRequestDTO(
    val athleteId: String,
    val planId: String,
    val targetDate: String,
    val startType: String,
)
