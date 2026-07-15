package io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.user

import java.io.Serializable

class TrainingPeaksUser(
    var userId: String,
    val isAthlete: Boolean,
    val isPremium: Boolean,
) : Serializable
