package io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.workout

enum class TPWorkoutSubType(
    val valueId: Int
) {
    // SWIM
    OPEN_WATER_SWIM(1),
    POOL_SWIM(2),
    // BIKE
    ROAD_BIKE(3),
    GRAVEL_BIKE(4),
    E_BIKE(8),
    VIRTUAL_BIKE(49),
    // MTB
    XC_MTB(14),
    ENDURO_MTB(15),
    E_MTB(19),
    // RUN
    ROAD_RUN(10),
    TRACK_RUN(11),
    TRAIL_RUN(12),
    INDOOR_RUN(13),
    VIRTUAL_RUN(50),
    // OTHER
    DAY_OFF(7),
    MOUNTAIN_BIKE(8),
    STRENGTH(9),
    // WALK
    HIKE(41),
   
    OTHER(100)
}