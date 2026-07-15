package io.github.costaalex.workoutrelay.infrastructure.platform.intervalsicu.configuration

import io.github.costaalex.workoutrelay.domain.Platform
import io.github.costaalex.workoutrelay.domain.config.AppConfigurationRepository
import io.github.costaalex.workoutrelay.domain.config.PlatformConfigurationRepository
import io.github.costaalex.workoutrelay.domain.config.PlatformInfo
import io.github.costaalex.workoutrelay.domain.config.PlatformInfoRepository
import io.github.costaalex.workoutrelay.domain.config.UpdateConfigurationRequest
import io.github.costaalex.workoutrelay.infrastructure.CatchFeignException
import io.github.costaalex.workoutrelay.infrastructure.PlatformException
import io.github.costaalex.workoutrelay.infrastructure.utils.Auth
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
@CacheConfig(cacheNames = ["platformInfoCache"])
class IntervalsConfigurationRepository(
    private val appConfigurationRepository: AppConfigurationRepository,
    private val intervalsAthleteApiClient: IntervalsAthleteApiClient,
    private val cacheManager: CacheManager,
) : PlatformConfigurationRepository, PlatformInfoRepository {
    override fun platform() = Platform.INTERVALS

    @CatchFeignException(platform = Platform.INTERVALS)
    override fun updateConfig(request: UpdateConfigurationRequest) {
        cacheManager.getCache("platformInfoCache")!!.evict(platform().key)
        val newConfig = getConfigToUpdate(request)
        validateConfiguration(newConfig)
        appConfigurationRepository.updateConfig(UpdateConfigurationRequest(newConfig))
    }

    @Cacheable(key = "'intervals'")
    override fun platformInfo(): PlatformInfo {
        val infoMap = mapOf(
            "isValid" to isValid(),
        )
        return PlatformInfo(infoMap)
    }

    fun getConfiguration(): IntervalsConfiguration {
        val config = appConfigurationRepository.getConfigurationByPrefix(platform().key)
        return IntervalsConfiguration(config)
    }

    private fun isValid(): Boolean {
        try {
            val currentConfig =
                appConfigurationRepository.getConfigurationByPrefix(platform().key)
            validateConfiguration(currentConfig.configMap)
            return true
        } catch (e: PlatformException) {
            return false
        }
    }

    private fun getConfigToUpdate(request: UpdateConfigurationRequest): Map<String, String?> {
        val currentConfig =
            appConfigurationRepository.getConfigurationByPrefix(platform().key)
        return currentConfig.configMap + request.getByPrefix(platform().key)
    }

    private fun validateConfiguration(newConfig: Map<String, String?>) {
        val intervalsConfig: IntervalsConfiguration
        try {
            intervalsConfig = IntervalsConfiguration(newConfig)
        } catch (e: NullPointerException) {
            throw PlatformException(platform(), "Access to the platform is not configured")
        }

        intervalsAthleteApiClient.getAthlete(
            intervalsConfig.athleteId,
            Auth.getAuthorizationHeader(intervalsConfig.apiKey)
        )
    }
}
