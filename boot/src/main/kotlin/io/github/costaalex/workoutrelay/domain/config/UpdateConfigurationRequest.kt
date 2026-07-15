package io.github.costaalex.workoutrelay.domain.config

data class UpdateConfigurationRequest(
    val configMap: Map<String, String?>
) {
    fun getByPrefix(prefix: String): Map<String, String?> {
        return configMap.filter { it.key.startsWith(prefix) }
    }
}
