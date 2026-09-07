package io.github.costaalex.workoutrelay.rest

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class RestResponseEntityExceptionHandlerTest {
    @Test
    fun `maps invalid requests to bad request`() {
        val response = RestResponseEntityExceptionHandler()
            .invalidRequest(IllegalArgumentException("Invalid date range"))

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body?.message).isEqualTo("Invalid date range")
    }
}