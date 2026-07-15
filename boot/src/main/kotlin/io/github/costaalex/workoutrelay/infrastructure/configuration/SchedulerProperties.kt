package io.github.costaalex.workoutrelay.infrastructure.configuration

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties("app.scheduler")
data class SchedulerProperties(
    @field:Min(1)
    @field:Max(24)
    val intervalHours: Long,
)
