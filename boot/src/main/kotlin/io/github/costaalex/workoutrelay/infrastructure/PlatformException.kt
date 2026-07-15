package io.github.costaalex.workoutrelay.infrastructure

import io.github.costaalex.workoutrelay.domain.Platform


class PlatformException(
    val platform: Platform,
    message: String
) : RuntimeException(message)
