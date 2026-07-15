package io.github.costaalex.workoutrelay.infrastructure.platform.intervalsicu.workout

class CreateEventRequestDTO(
    val start_date_local: String,
    val name: String,
    val type: String,
    val category: String,
    val description: String,
)
