package io.github.costaalex.workoutrelay.rest.configuration

import io.github.costaalex.workoutrelay.domain.TrainingType

class TrainingTypeDTO(
    val title: String,
    val value: String
) {
    constructor(trainingType: TrainingType) : this(trainingType.title, trainingType.name)
}
