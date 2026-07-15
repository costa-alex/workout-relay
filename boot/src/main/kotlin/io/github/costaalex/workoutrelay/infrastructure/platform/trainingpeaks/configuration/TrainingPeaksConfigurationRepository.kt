package io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.configuration

import io.github.costaalex.workoutrelay.domain.Platform
import io.github.costaalex.workoutrelay.domain.config.AppConfigurationRepository
import io.github.costaalex.workoutrelay.domain.config.PlatformConfigurationRepository
import io.github.costaalex.workoutrelay.domain.config.UpdateConfigurationRequest
import io.github.costaalex.workoutrelay.infrastructure.CatchFeignException
import io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.token.TrainingPeaksTokenApiClient
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service

@Service
class TrainingPeaksConfigurationRepository(
    private val appConfigurationRepository: AppConfigurationRepository,
    private val trainingPeaksTokenApiClient: TrainingPeaksTokenApiClient,
    private val cacheManager: CacheManager,
) : PlatformConfigurationRepository {
    override fun platform() = Platform.TRAINING_PEAKS

    @CatchFeignException(platform = Platform.TRAINING_PEAKS)
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

    fun getConfiguration(): TrainingPeaksConfiguration {
        val config = appConfigurationRepository.getConfigurationByPrefix(platform().key)
        return TrainingPeaksConfiguration(config)
    }

    fun isValid(): Boolean {
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
        val tpConfig = TrainingPeaksConfiguration(newConfig)
        if (!tpConfig.canValidate() && ignoreEmpty) {
            return
        }
        trainingPeaksTokenApiClient.getToken(tpConfig.authCookie ?: "")
    }
}
