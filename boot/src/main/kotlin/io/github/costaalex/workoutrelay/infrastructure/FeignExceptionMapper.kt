package io.github.costaalex.workoutrelay.infrastructure

import feign.FeignException
import feign.RetryableException
import feign.codec.DecodeException
import io.github.costaalex.workoutrelay.domain.Platform
import org.springframework.stereotype.Component

@Component
class FeignExceptionMapper {

    fun map(
        platform: Platform,
        exception: FeignException,
    ): PlatformException {
        val upstreamStatus =
            exception.status()
                .takeIf { it >= 0 }

        val codeAndMessage =
            when {
                exception is DecodeException ->
                    PlatformErrorCode.INVALID_RESPONSE to
                        (
                            "${platform.title} returned a response that could not be processed"
                        )

                exception is RetryableException ->
                    PlatformErrorCode.UNAVAILABLE to
                        (
                            "${platform.title} is temporarily unavailable"
                        )

                exception.status() in setOf(401, 403) ->
                    PlatformErrorCode.AUTHENTICATION_FAILED to
                        (
                            "Authentication with ${platform.title} failed"
                        )

                exception.status() == 404 ->
                    PlatformErrorCode.RESOURCE_NOT_FOUND to
                        (
                            "The requested resource was not found in ${platform.title}"
                        )

                exception.status() == 429 ->
                    PlatformErrorCode.RATE_LIMITED to
                        (
                            "${platform.title} request limit was exceeded"
                        )

                exception.status() in 500..599 ->
                    PlatformErrorCode.UNAVAILABLE to
                        (
                            "${platform.title} is temporarily unavailable"
                        )

                else ->
                    PlatformErrorCode.REQUEST_FAILED to
                        (
                            "Unable to complete the request to " + platform.title
                        )
            }

        return PlatformException(
            platform = platform,
            code = codeAndMessage.first,
            upstreamStatus = upstreamStatus,
            message = codeAndMessage.second,
            cause = exception,
        )
    }
}