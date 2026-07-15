package io.github.costaalex.workoutrelay.infrastructure.platform.intervalsicu.activity

import io.github.costaalex.workoutrelay.domain.activity.Activity
import io.github.costaalex.workoutrelay.infrastructure.platform.intervalsicu.IntervalsActivityDTO

class IntervalsToActivityMapper(
    private val eventDTO: IntervalsActivityDTO
) {
    fun mapToActivity(): Activity {
        return Activity(
            eventDTO.start_date_local,
            eventDTO.mapType(),
            eventDTO.name,
            null
        )
    }
}
