package io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.configuration

import io.github.costaalex.workoutrelay.domain.Platform
import io.github.costaalex.workoutrelay.domain.config.AppConfiguration

data class TrainingPeaksConfiguration(
    val authCookie: String?,
    val planDaysShift: Long,
) {
    companion object {
        private val authCookieKey = "${Platform.TRAINING_PEAKS.key}.auth-cookie"
        private val planDaysShiftKey = "${Platform.TRAINING_PEAKS.key}.copy-plan-days-shift"
    }

    constructor(appConfiguration: AppConfiguration) : this(appConfiguration.configMap)

    constructor(map: Map<String, String?>) : this(
        map[authCookieKey],
        map[planDaysShiftKey]!!.toLong(),
    )

    fun canValidate(): Boolean {
        return authCookie != null
    }
}
