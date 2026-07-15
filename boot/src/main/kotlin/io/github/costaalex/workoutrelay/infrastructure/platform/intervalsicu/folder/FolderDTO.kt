package io.github.costaalex.workoutrelay.infrastructure.platform.intervalsicu.folder

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

class FolderDTO(
    val id: String,
    val type: String,
    val name: String,
    @JsonProperty("start_date_local")
    val startDateLocal: LocalDate?,
    val num_workouts: Int,
)
