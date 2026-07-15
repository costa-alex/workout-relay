package io.github.costaalex.workoutrelay.domain.activity

import io.github.costaalex.workoutrelay.domain.Platform
import io.github.costaalex.workoutrelay.domain.TrainingType
import java.time.LocalDate

interface ActivityRepository {
    fun platform(): Platform

    fun getActivities(startDate: LocalDate, endDate: LocalDate, types: List<TrainingType>): List<Activity>

    fun saveActivities(activities: List<Activity>)
}
