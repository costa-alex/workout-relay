package org.freekode.tp2intervals.rest.workout

import org.freekode.tp2intervals.app.workout.schedule.C2CScheduledRequest
import org.freekode.tp2intervals.app.workout.schedule.WorkoutScheduledJob
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class WorkoutScheduledJobController(
    private val workoutScheduledJob: WorkoutScheduledJob
) {
    @PostMapping(
        "/api/workout/copy-calendar-to-calendar/schedule"
    )
    fun scheduleC2CRequest(
        @RequestBody request: C2CScheduledRequest
    ) {
        workoutScheduledJob.addRequest(request)
    }

    @GetMapping(
        "/api/workout/copy-calendar-to-calendar/schedule"
    )
    fun getScheduleRequests() =
        workoutScheduledJob.getRequests()

    @PostMapping(
        "/api/workout/copy-calendar-to-calendar/schedule/{id}/run"
    )
    fun runScheduleRequest(
        @PathVariable id: Int
    ) = workoutScheduledJob.runRequest(id)

    @DeleteMapping(
        "/api/workout/copy-calendar-to-calendar/schedule/{id}"
    )
    fun deleteScheduleRequest(
        @PathVariable id: Int
    ) {
        workoutScheduledJob.deleteRequest(id)
    }
}
