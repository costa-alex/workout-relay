package io.github.costaalex.workoutrelay.infrastructure.platform.trainerroad.workout

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import kotlin.math.roundToInt

class TRWorkoutResponseDTO(
    @JsonProperty("Workout")
    @JsonAlias("workout")
    val workout: TRWorkout,
) {

    class TRWorkout(
        @JsonProperty("Details")
        @JsonAlias("details")
        val details: TrainerRoadWorkoutDetailsDTO,

        @JsonProperty("IntervalData")
        @JsonAlias("intervalData")
        val intervalData: List<IntervalsDataDTO> =
            emptyList(),

        @JsonProperty("WorkoutData")
        @JsonAlias("workoutData")
        val workoutData: List<WorkoutDataPointDTO> =
            emptyList(),
    ) {
        @JsonIgnore
        val additionalProperties:
            MutableMap<String, Any?> = linkedMapOf()

        @JsonAnySetter
        fun captureAdditionalProperty(
            name: String,
            value: Any?
        ) {
            additionalProperties[name] = value
        }
    }

    class WorkoutDataPointDTO(
        @JsonProperty("Tick")
        @JsonAlias("tick")
        val tick: Int,

        @JsonProperty("MemberFtpPercent")
        @JsonAlias("memberFtpPercent")
        val memberFtpPower: Double? = null,

        @JsonProperty("FtpPercent")
        @JsonAlias("ftpPercent")
        val ftpPercent: Double? = null,
    )

    class IntervalsDataDTO(
        @JsonProperty("Start")
        @JsonAlias("start")
        val start: Double,

        @JsonProperty("End")
        @JsonAlias("end")
        val end: Double,

        @JsonProperty("Name")
        @JsonAlias("name")
        val name: String,

        @JsonProperty("IsFake")
        @JsonAlias("isFake")
        val isFake: Boolean,

        @JsonProperty("TestInterval")
        @JsonAlias("testInterval")
        val testInterval: Boolean,

        @JsonProperty("StartTargetPowerPercent")
        @JsonAlias("startTargetPowerPercent")
        val startTargetPowerPercent: Double = 0.0,

        @JsonProperty("StartTarget")
        @JsonAlias("startTarget")
        val startTarget: List<Double>? = null,
    ) {
        @JsonIgnore
        val additionalProperties:
            MutableMap<String, Any?> = linkedMapOf()

        @JsonAnySetter
        fun captureAdditionalProperty(
            name: String,
            value: Any?
        ) {
            additionalProperties[name] = value
        }

        fun targetStart(): Int =
            (
                startTarget?.firstOrNull()
                    ?: startTargetPowerPercent
            ).roundToInt()
    }
}