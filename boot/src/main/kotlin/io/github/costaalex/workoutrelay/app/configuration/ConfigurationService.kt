package io.github.costaalex.workoutrelay.app.configuration

import io.github.costaalex.workoutrelay.domain.Platform
import io.github.costaalex.workoutrelay.domain.config.AppConfiguration
import io.github.costaalex.workoutrelay.domain.config.AppConfigurationRepository
import io.github.costaalex.workoutrelay.domain.config.DebugModeService
import io.github.costaalex.workoutrelay.domain.config.PlatformConfigurationRepository
import io.github.costaalex.workoutrelay.domain.config.PlatformInfo
import io.github.costaalex.workoutrelay.domain.config.PlatformInfoRepository
import io.github.costaalex.workoutrelay.domain.config.UpdateConfigurationRequest
import io.github.costaalex.workoutrelay.infrastructure.PlatformException
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager

@Service
class ConfigurationService(
    private val platformConfigurationRepositories: List<PlatformConfigurationRepository>,
    platformInfoRepositories: List<PlatformInfoRepository>,
    private val appConfigurationRepository: AppConfigurationRepository,
    private val debugModeService: DebugModeService,
    private val cacheManager: CacheManager,
) {
    private val platformInfoRepositoryMap = platformInfoRepositories.associateBy { it.platform() }
    private val log = LoggerFactory.getLogger(this.javaClass)
    
    fun getConfiguration(key: String): String? = appConfigurationRepository.getConfiguration(key)

    fun getConfigurations(): AppConfiguration = appConfigurationRepository.getConfigurations()

    fun updateConfiguration(request: UpdateConfigurationRequest): List<String> {
        val errors = platformConfigurationRepositories.mapNotNull { updateConfiguration(request, it) }
        handleDebugModeIfNecessary(request)
        return errors
    }

   fun platformInfo(): Map<Platform, PlatformInfo> =
    platformInfoRepositoryMap.entries.associate { entry ->
        val platform = entry.key
        val repository = entry.value

        val info = try {
            repository.platformInfo()
        } catch (exception: Exception) {
            log.warn(
                "Unable to validate connection to {}",
                platform.title,
                exception
            )

            PlatformInfo(
                mapOf("isValid" to false)
            )
        }

        platform to info
    }

    fun platformInfo(platform: Platform): PlatformInfo {
        return platformInfoRepositoryMap[platform]!!.platformInfo()
    }
 
    fun refreshPlatformInfo(): Map<Platform, PlatformInfo> {
        cacheManager
            .getCache("platformInfoCache")
            ?.clear()

        return platformInfo()
    }

    private fun handleDebugModeIfNecessary(request: UpdateConfigurationRequest) {
        debugModeService.handleDebugMode(request.configMap)
    }

    private fun updateConfiguration(
        request: UpdateConfigurationRequest,
        repository: PlatformConfigurationRepository
    ): String? {
        return try {
            repository.updateConfig(request)
            null
        } catch (e: PlatformException) {
            "${e.platform.title}: ${e.message}"
        } catch (e: Exception) {
            e.message
        }
    }
}
