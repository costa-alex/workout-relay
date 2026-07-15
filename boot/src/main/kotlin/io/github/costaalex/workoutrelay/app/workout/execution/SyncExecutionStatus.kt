package io.github.costaalex.workoutrelay.app.workout.execution

enum class SyncExecutionStatus {
    RUNNING,
    SUCCESS,
    NO_CHANGES,
    PARTIAL_SUCCESS,
    FAILED,
    INTERRUPTED
}