package io.github.costaalex.workoutrelay.app.workout

import io.github.costaalex.workoutrelay.domain.ExternalData
import io.github.costaalex.workoutrelay.domain.Platform
import io.github.costaalex.workoutrelay.domain.librarycontainer.LibraryContainer

data class CopyFromLibraryToLibraryRequest(
    val workoutExternalData: ExternalData,
    val targetLibraryContainer: LibraryContainer,
    val sourcePlatform: Platform,
    val targetPlatform: Platform,
)
