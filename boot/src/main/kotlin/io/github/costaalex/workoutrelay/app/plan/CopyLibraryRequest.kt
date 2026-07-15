package io.github.costaalex.workoutrelay.app.plan

import io.github.costaalex.workoutrelay.domain.Platform
import io.github.costaalex.workoutrelay.domain.librarycontainer.LibraryContainer
import io.github.costaalex.workoutrelay.domain.workout.structure.StepModifier

data class CopyLibraryRequest(
    val libraryContainer: LibraryContainer,
    val newName: String,
    val stepModifier: StepModifier,
    val sourcePlatform: Platform,
    val targetPlatform: Platform,
)
