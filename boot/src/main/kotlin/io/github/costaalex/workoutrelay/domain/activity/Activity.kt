package io.github.costaalex.workoutrelay.domain.activity

import io.github.costaalex.workoutrelay.domain.TrainingType
import io.github.costaalex.workoutrelay.infrastructure.utils.Base64
import org.springframework.core.io.Resource
import java.time.LocalDateTime

data class Activity(
    val startedAt: LocalDateTime,
    val type: TrainingType,
    val title: String,
    val resource: String?
) {
    fun withResource(resource: Resource) =
        Activity(startedAt, type, title, Base64.encodeToString(resource))
}
