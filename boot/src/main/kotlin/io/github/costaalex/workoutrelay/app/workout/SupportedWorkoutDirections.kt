package io.github.costaalex.workoutrelay.app.workout

import io.github.costaalex.workoutrelay.domain.Platform

private val calendarDirections = setOf(
    Platform.TRAINER_ROAD to Platform.TRAINING_PEAKS,
    Platform.TRAINER_ROAD to Platform.INTERVALS,
    Platform.TRAINING_PEAKS to Platform.INTERVALS,
    Platform.INTERVALS to Platform.TRAINING_PEAKS,
)

fun requireSupportedCalendarDirection(
    sourcePlatform: Platform,
    targetPlatform: Platform,
) {
    require(sourcePlatform to targetPlatform in calendarDirections) {
        "Calendar synchronization from $sourcePlatform to $targetPlatform is not supported"
    }
}