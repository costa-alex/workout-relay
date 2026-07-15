package io.github.costaalex.workoutrelay.app.plan

import io.github.costaalex.workoutrelay.domain.ExternalData
import io.github.costaalex.workoutrelay.domain.Platform

data class DeleteLibraryRequest(
    val externalData: ExternalData,
    val platform: Platform,
)
