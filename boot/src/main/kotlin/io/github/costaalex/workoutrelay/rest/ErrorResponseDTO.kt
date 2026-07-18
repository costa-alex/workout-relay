package io.github.costaalex.workoutrelay.rest

data class ErrorResponseDTO(
    val platform: String?,
    val message: String,
    val code: String? = null,
) {

    constructor(
        message: String,
    ) : this(
        platform = null,
        message = message,
        code = null,
    )
}