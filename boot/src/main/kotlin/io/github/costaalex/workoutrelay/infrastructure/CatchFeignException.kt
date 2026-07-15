package io.github.costaalex.workoutrelay.infrastructure

import io.github.costaalex.workoutrelay.domain.Platform

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class CatchFeignException(
    val platform: Platform
)
