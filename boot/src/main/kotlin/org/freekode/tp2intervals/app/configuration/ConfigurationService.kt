package org.freekode.tp2intervals.app.configuration

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.config.AppConfiguration
import org.freekode.tp2intervals.domain.config.AppConfigurationRepository
import org.freekode.tp2intervals.domain.config.DebugModeService
import org.freekode.tp2intervals.domain.config.PlatformConfigurationRepository
import org.freekode.tp2intervals.domain.config.PlatformInfo
import org.freekode.tp2intervals.domain.config.PlatformInfoRepository
import org.freekode.tp2intervals.domain.config.UpdateConfigurationRequest
import org.freekode.tp2intervals.infrastructure.PlatformException
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
