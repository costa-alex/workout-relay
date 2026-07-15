package io.github.costaalex.workoutrelay.infrastructure.platform.trainerroad.workout

import com.fasterxml.jackson.annotation.JsonProperty

class TRFindWorkoutsResponseDTO(
    @JsonProperty("Workouts")
    val workouts: List<TrainerRoadWorkoutDetailsDTO>,
)
