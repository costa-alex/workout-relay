package io.github.costaalex.workoutrelay.infrastructure.configuration

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties("app.sync-history")
data class SyncHistoryProperties(

    @field:Min(1)
    @field:Max(10_000)
    val retentionLimit: Int = 100
)