package io.github.costaalex.workoutrelay.rest

import io.github.costaalex.workoutrelay.infrastructure.PlatformException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import io.github.costaalex.workoutrelay.app.workout.schedule.ScheduleAlreadyRunningException
import io.github.costaalex.workoutrelay.infrastructure.PlatformErrorCode

@ControllerAdvice
class RestResponseEntityExceptionHandler : ResponseEntityExceptionHandler() {
    @ExceptionHandler(PlatformException::class)
    fun platformException(
        exception: PlatformException,
    ): ResponseEntity<ErrorResponseDTO> {
        val status =
            when (exception.code) {
                PlatformErrorCode.RATE_LIMITED,
                PlatformErrorCode.UNAVAILABLE ->
                    HttpStatus.SERVICE_UNAVAILABLE

                PlatformErrorCode.AUTHENTICATION_FAILED,
                PlatformErrorCode.RESOURCE_NOT_FOUND,
                PlatformErrorCode.INVALID_RESPONSE,
                PlatformErrorCode.REQUEST_FAILED ->
                    HttpStatus.BAD_GATEWAY
            }

        return ResponseEntity
            .status(status)
            .body(
                ErrorResponseDTO(
                    platform =
                        exception.platform.title,
                    message =
                        exception.message
                            ?: "An external platform error occurred",
                    code =
                        exception.code.name,
                )
            )
    }

    @ExceptionHandler(ScheduleAlreadyRunningException::class)
    fun handleScheduleAlreadyRunning(
        exception: ScheduleAlreadyRunningException
    ): ResponseEntity<ErrorResponseDTO> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(
                ErrorResponseDTO(
                    exception.message
                        ?: "Scheduled sync is already running"
                )
            )
    }
}
