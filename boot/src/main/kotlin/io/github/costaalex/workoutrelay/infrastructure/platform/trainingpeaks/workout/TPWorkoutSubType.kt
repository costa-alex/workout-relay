package io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.workout

enum class TPWorkoutSubType(
    val valueId: Int
) {
    SWIM(1),
    RIDE(2),
    RUN(3),
    DAY_OFF(7),
    MOUNTAIN_BIKE(8),
    WEIGHT(9),
    WALK(13),
    VIRTUAL_RIDE(49),
    OTHER(100)
}