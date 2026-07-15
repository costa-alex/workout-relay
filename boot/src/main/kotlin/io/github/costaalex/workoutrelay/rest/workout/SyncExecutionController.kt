package io.github.costaalex.workoutrelay.rest.workout

import io.github.costaalex.workoutrelay.app.workout.execution.SyncExecutionService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SyncExecutionController(
    private val syncExecutionService: SyncExecutionService
) {

    @GetMapping("/api/sync-executions")
    fun getRecentExecutions() =
        syncExecutionService.getRecentExecutions()
}