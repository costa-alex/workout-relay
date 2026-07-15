package io.github.costaalex.workoutrelay.domain.config

import io.github.costaalex.workoutrelay.domain.Platform

interface PlatformConfigurationRepository {
    fun platform(): Platform

    fun updateConfig(request: UpdateConfigurationRequest)
}
