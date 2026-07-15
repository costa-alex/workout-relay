package io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.plan

import java.time.LocalDateTime

class ApplyTPPlanResponseDTO(
    val appliedPlanId: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
)
