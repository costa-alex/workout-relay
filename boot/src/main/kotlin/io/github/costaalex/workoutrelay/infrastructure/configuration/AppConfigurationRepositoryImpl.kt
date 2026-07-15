package io.github.costaalex.workoutrelay.infrastructure.configuration

import io.github.costaalex.workoutrelay.domain.config.AppConfiguration
import io.github.costaalex.workoutrelay.domain.config.AppConfigurationRepository
import io.github.costaalex.workoutrelay.domain.config.UpdateConfigurationRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class AppConfigurationRepositoryImpl(
    private val configurationCrudRepository: ConfigurationCrudRepository,
) : AppConfigurationRepository {

    override fun getConfiguration(key: String): String? {
        return configurationCrudRepository.findByIdOrNull(key)?.value
    }

    override fun getConfigurations(): AppConfiguration {
        return toDomain(configurationCrudRepository.findAll())
    }

    override fun getConfigurationByPrefix(prefix: String): AppConfiguration {
        return toDomain(configurationCrudRepository.findByKeyLike("$prefix%"))
    }

    override fun updateConfig(request: UpdateConfigurationRequest) {
        request.configMap.forEach { (key, value) ->
            if (value == null) {
                configurationCrudRepository.deleteById(key)
            } else {
                configurationCrudRepository.save(AppConfigurationEntryEntity(key, value))
            }
        }
    }

    private fun toDomain(entities: Iterable<AppConfigurationEntryEntity>): AppConfiguration {
        val configMap = entities.associateBy { it.key!! }.mapValues { it.value.value!! }
        return AppConfiguration(configMap)
    }
}
