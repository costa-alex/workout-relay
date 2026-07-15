package io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.library

import java.io.Serializable

class TPWorkoutLibraryDTO(
    val exerciseLibraryId: String,
    val libraryName: String,
    val ownerName: String,
) : Serializable
