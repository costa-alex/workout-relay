package io.github.costaalex.workoutrelay.domain

enum class TrainingType(val title: String) {
    BIKE("Ride"),
    MTB("MTB"),
    VIRTUAL_BIKE("Virtual Bike"),
    RUN("Run"),
    SWIM("Swim"),
    WALK("Walk"),
    STRENGTH("Strength"),
    NOTE("Note"),
    UNKNOWN("Unknown");

    companion object {
        val DEFAULT_LIST = listOf(BIKE, VIRTUAL_BIKE, MTB, RUN, SWIM)
    }
}
