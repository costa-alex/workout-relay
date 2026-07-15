package io.github.costaalex.workoutrelay.infrastructure.platform.trainerroad.configuration

import io.github.costaalex.workoutrelay.domain.Platform
import io.github.costaalex.workoutrelay.domain.config.AppConfiguration

data class TrainerRoadConfiguration(
    val authCookie: String?,
    val removeHtmlTags: Boolean,
) {
    companion object {
        private val authCookieKey = "${Platform.TRAINER_ROAD.key}.auth-cookie"
        private val removeHtmlTagsKey = "${Platform.TRAINER_ROAD.key}.remove-html-tags"
    }

    constructor(appConfiguration: AppConfiguration) : this(appConfiguration.configMap)

    constructor(map: Map<String, String?>) : this(
        map[authCookieKey],
        map[removeHtmlTagsKey].toBoolean(),
        )

    fun canValidate(): Boolean {
        return authCookie != null
    }
}
