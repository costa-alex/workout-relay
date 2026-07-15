package io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.workout

enum class TPWorkoutType(
    val valueId: Int
) {
    SWIM(1),
    BIKE(2),
    RUN(3),
    DAY_OFF(7),
    WEIGHT(9),
    OTHER(100)
}