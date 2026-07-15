package io.github.costaalex.workoutrelay.rest

import io.github.costaalex.workoutrelay.infrastructure.PlatformException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import io.github.costaalex.workoutrelay.app.workout.schedule.ScheduleAlreadyRunningException

@ControllerAdvice
class RestResponseEntityExceptionHandler : ResponseEntityExceptionHandler() {
    @ExceptionHandler(PlatformException::class)
    fun platformException(
        exception: PlatformException,
        request: WebRequest
    ): ResponseEntity<ErrorResponseDTO> {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ErrorResponseDTO(
                    exception.platform.title,
                    exception.message
                        ?: "An external platform error occurred"
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
