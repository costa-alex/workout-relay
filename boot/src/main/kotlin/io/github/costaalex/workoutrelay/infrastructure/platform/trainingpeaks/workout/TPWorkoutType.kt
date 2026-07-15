package io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.workout

enum class TPWorkoutType(
    val valueId: Int
) {
    SWIM(1),
    BIKE(2),
    RUN(3),
    DAY_OFF(7),
    MTB_BIKE(8),
    STRENGTH(9),
    WALK(13),
    OTHER(100)
}