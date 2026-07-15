package io.github.costaalex.workoutrelay.domain.workout

import io.github.costaalex.workoutrelay.infrastructure.utils.Base64
import org.springframework.core.io.Resource

data class Attachment(
    val name: String,
    val content: String,
) {
    constructor(name: String, resource: Resource) : this(name, Base64.encodeToString(resource))
}