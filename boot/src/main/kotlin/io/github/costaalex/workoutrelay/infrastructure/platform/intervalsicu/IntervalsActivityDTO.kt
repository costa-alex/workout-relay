package io.github.costaalex.workoutrelay.infrastructure.platform.intervalsicu

import java.time.LocalDateTime
import io.github.costaalex.workoutrelay.domain.TrainingType
import io.github.costaalex.workoutrelay.infrastructure.platform.intervalsicu.workout.IntervalsTrainingTypeMapper

class IntervalsActivityDTO(
    val id: String,
    val name: String,
    val description: String?,
    val start_date_local: LocalDateTime,
    val type: String?,
    val moving_time: Long,
    val icu_training_load: Int?,
) {
    fun mapType(): TrainingType = type?.let { IntervalsTrainingTypeMapper.getByIntervalsType(it) } ?: TrainingType.UNKNOWN
}
