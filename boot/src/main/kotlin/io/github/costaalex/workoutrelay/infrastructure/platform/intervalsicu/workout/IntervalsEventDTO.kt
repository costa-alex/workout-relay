package io.github.costaalex.workoutrelay.infrastructure.platform.intervalsicu.workout

import java.time.Duration
import java.time.LocalDateTime
import io.github.costaalex.workoutrelay.domain.TrainingType

class IntervalsEventDTO(
    val id: Long,
    val name: String,
    val description: String?,
    val start_date_local: LocalDateTime,
    val category: String,
    val type: String?,
    val moving_time: Long?,
    val icu_training_load: Int?,
    val workout_doc: IntervalsWorkoutDocDTO?,
) {

    fun mapType(): TrainingType = type?.let { IntervalsTrainingTypeMapper.getByIntervalsType(it) } ?: TrainingType.UNKNOWN
    fun mapSubType(): TrainingType = type?.let { IntervalsTrainingTypeMapper.getByIntervalsType(it) } ?: TrainingType.UNKNOWN

    fun mapDuration(): Duration? = moving_time?.let { Duration.ofSeconds(it) }

    fun isWorkout() = category == "WORKOUT"
}
