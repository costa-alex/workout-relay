package io.github.costaalex.workoutrelay.infrastructure

import io.github.costaalex.workoutrelay.domain.Platform

class PlatformException(
    val platform: Platform,
    val code: PlatformErrorCode,
    val upstreamStatus: Int? = null,
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {

    /*
     * Mantém compatibilidade com código e testes que ainda usam
     * PlatformException(platform, message).
     */
    constructor(
        platform: Platform,
        message: String,
    ) : this(
        platform = platform,
        code = PlatformErrorCode.REQUEST_FAILED,
        message = message,
    )
}