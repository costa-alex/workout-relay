package io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.plan

import java.time.LocalDate

class TPPlanDto(
    val planId: String,
    val title: String,
    val workoutCount: Int,
    val startDate: LocalDate?,
)
