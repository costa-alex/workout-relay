package io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.user

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TrainingPeaksUserDTO(
    val user: TPUserDTO,
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class TPUserDTO(
        val userId: Long,
        val settings: TPUserSettingsDTO,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class TPUserSettingsDTO(
        val account: TPUserAccountDTO,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class TPUserAccountDTO(
        val isAthlete: Boolean,
        val isPremium: Boolean,
        val premiumTrial: Boolean = false,
    )
}