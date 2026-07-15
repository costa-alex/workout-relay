package io.github.costaalex.workoutrelay.app.workout.schedule

class ScheduleAlreadyRunningException(
    val scheduleId:package io.github.costaalex.workoutrelay.app.workout.schedule

class ScheduleAlreadyRunningException(
    val scheduleId: Int
) : IllegalStateException(
    "Scheduled sync $scheduleId is already running"
)