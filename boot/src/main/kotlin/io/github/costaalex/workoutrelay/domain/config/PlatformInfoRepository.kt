package io.github.costaalex.workoutrelay.domain.config

import io.github.costaalex.workoutrelay.domain.Platform

interface PlatformInfoRepository {
    fun platform(): Platform

    fun platformInfo(): PlatformInfo
}
