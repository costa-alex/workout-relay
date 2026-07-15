package io.github.costaalex.workoutrelay.infrastructure.platform.trainerroad.configuration

import io.github.costaalex.workoutrelay.domain.Platform
import io.github.costaalex.workoutrelay.domain.config.AppConfigurationRepository
import io.github.costaalex.workoutrelay.domain.config.PlatformConfigurationRepository
import io.github.costaalex.workoutrelay.domain.config.PlatformInfo
import io.github.costaalex.workoutrelay.domain.config.PlatformInfoRepository
import io.github.costaalex.workoutrelay.domain.config.UpdateConfigurationRequest
import io.github.costaalex.workoutrelay.infrastructure.CatchFeignException
import io.github.costaalex.workoutrelay.infrastructure.PlatformException
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
@CacheConfig(cacheNames = ["platformInfoCache"])
class TrainerRoadConfigurationRepository(
    private val appConfigurationRepository: AppConfigurationRepository,
    private val trainerRoadValidationApiClient: TrainerRoadValidationApiClient,
    private val cacheManager: CacheManager,
) : PlatformConfigurationRepository, PlatformInfoRepository {
    override fun platform() = Platform.TRAINER_ROAD

    @CatchFeignException(platform = Platform.TRAINER_ROAD)
    override fun updateConfig(request: UpdateConfigurationRequest) {
        cacheManager.getCache("platformInfoCache")!!.evict(platform().key)
        val updatedConfig = request.getByPrefix(platform().key)
        if (updatedConfig.isEmpty()) {
            return
        }
        val currentConfig =
            appConfigurationRepository.getConfigurationByPrefix(platform().key)
        val newConfig = currentConfig.configMap + updatedConfig
        validateConfiguration(newConfig, true)
        appConfigurationRepository.updateConfig(UpdateConfigurationRequest(newConfig))
    }

    @Cacheable(key = "'trainer-road'")
    override fun platformInfo(): PlatformInfo {
        val infoMap = mapOf(
            "isValid" to isValid(),
        )
        return PlatformInfo(infoMap)
    }

    fun getConfiguration(): TrainerRoadConfiguration {
        val config = appConfigurationRepository.getConfigurationByPrefix(platform().key)
        return TrainerRoadConfiguration(config)
    }

    private fun isValid(): Boolean {
        try {
            val currentConfig =
                appConfigurationRepository.getConfigurationByPrefix(platform().key)
            validateConfiguration(currentConfig.configMap, false)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    private fun validateConfiguration(newConfig: Map<String, String?>, ignoreEmpty: Boolean) {
        val config = TrainerRoadConfiguration(newConfig)
        if (!config.canValidate() && ignoreEmpty) {
            return
        }
        val member = trainerRoadValidationApiClient.getMember(config.authCookie ?: "")
        if (member == null || member.MemberId == -1L) {
            throw PlatformException(Platform.TRAINER_ROAD, "Access Denied")
        }
    }
}
