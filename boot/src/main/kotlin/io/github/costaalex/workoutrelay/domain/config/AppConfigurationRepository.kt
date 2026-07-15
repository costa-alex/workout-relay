package io.github.costaalex.workoutrelay.domain.config

interface AppConfigurationRepository {
    fun getConfiguration(key: String): String?

    fun getConfigurations(): AppConfiguration

    fun getConfigurationByPrefix(prefix: String): AppConfiguration

    fun updateConfig(request: UpdateConfigurationRequest)
}
