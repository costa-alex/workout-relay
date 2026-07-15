package io.github.costaalex.workoutrelay.app.plan

import io.github.costaalex.workoutrelay.domain.Platform

data class CreateLibraryContainerRequest(
    val name: String,
    val platform: Platform,
)