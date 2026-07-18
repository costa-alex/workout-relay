package io.github.costaalex.workoutrelay.infrastructure

import feign.FeignException
import feign.codec.DecodeException
import io.github.costaalex.workoutrelay.domain.Platform
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class FeignExceptionMapperTest {

    private val mapper =
        FeignExceptionMapper()

    @Test
    fun `should map authentication error`() {
        val feignException =
            mock(FeignException::class.java)

        `when`(feignException.status())
            .thenReturn(401)

        val result =
            mapper.map(
                Platform.TRAINING_PEAKS,
                feignException,
            )

        assertEquals(
            PlatformErrorCode.AUTHENTICATION_FAILED,
            result.code,
        )

        assertEquals(
            401,
            result.upstreamStatus,
        )

        assertSame(
            feignException,
            result.cause,
        )
    }

    @Test
    fun `should map rate limit error`() {
        val feignException =
            mock(FeignException::class.java)

        `when`(feignException.status())
            .thenReturn(429)

        val result =
            mapper.map(
                Platform.INTERVALS,
                feignException,
            )

        assertEquals(
            PlatformErrorCode.RATE_LIMITED,
            result.code,
        )
    }

    @Test
    fun `should map invalid response`() {
        val decodeException =
            mock(DecodeException::class.java)

        `when`(decodeException.status())
            .thenReturn(200)

        val result =
            mapper.map(
                Platform.TRAINING_PEAKS,
                decodeException,
            )

        assertEquals(
            PlatformErrorCode.INVALID_RESPONSE,
            result.code,
        )

        assertEquals(
            "TrainingPeaks returned a response that could not be processed",
            result.message,
        )
    }

    @Test
    fun `should map external server error`() {
        val feignException =
            mock(FeignException::class.java)

        `when`(feignException.status())
            .thenReturn(503)

        val result =
            mapper.map(
                Platform.TRAINER_ROAD,
                feignException,
            )

        assertEquals(
            PlatformErrorCode.UNAVAILABLE,
            result.code,
        )
    }
}