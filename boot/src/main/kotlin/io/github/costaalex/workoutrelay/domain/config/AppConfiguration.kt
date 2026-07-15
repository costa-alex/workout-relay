package io.github.costaalex.workoutrelay.domain.config

class AppConfiguration(
    val configMap: Map<String, String>,
) {
    fun get(key: String): String = configMap[key]!!
    fun find(key: String): String? = configMap[key]
}
