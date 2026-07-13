package org.freekode.tp2intervals.app.workout.execution

enum class SyncExecutionStatus {
    RUNNING,
    SUCCESS,
    NO_CHANGES,
    PARTIAL_SUCCESS,
    FAILED,
    INTERRUPTED
}