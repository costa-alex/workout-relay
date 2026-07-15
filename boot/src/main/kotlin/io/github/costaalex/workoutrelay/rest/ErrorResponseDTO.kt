package io.github.costaalex.workoutrelay.rest

data class ErrorResponseDTO(
    val platform: String?,
    val message: String,
) {
    constructor(message: String) : this(null, message)
}
