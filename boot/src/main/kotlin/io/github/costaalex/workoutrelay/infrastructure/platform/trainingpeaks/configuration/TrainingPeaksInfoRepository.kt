package io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.configuration

import io.github.costaalex.workoutrelay.domain.Platform
import io.github.costaalex.workoutrelay.domain.config.PlatformInfo
import io.github.costaalex.workoutrelay.domain.config.PlatformInfoRepository
import io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.user.TrainingPeaksUserRepository
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
@CacheConfig(cacheNames = ["platformInfoCache"])
class TrainingPeaksInfoRepository(
    private val trainingPeaksConfigurationRepository: TrainingPeaksConfigurationRepository,
    private val trainingPeaksUserRepository: TrainingPeaksUserRepository,
) : PlatformInfoRepository {
    override fun platform() = Platform.TRAINING_PEAKS

    @Cacheable(key = "'training-peaks'")
    override fun platformInfo(): PlatformInfo {
        val isValid = trainingPeaksConfigurationRepository.isValid()
        if (!isValid) {
            return PlatformInfo(mapOf("isValid" to false))
        }

        val user = trainingPeaksUserRepository.getUser()
        val infoMap = mapOf(
            "isValid" to true,
            "isAthlete" to user.isAthlete,
            "isPremium" to user.isPremium
        )
        return PlatformInfo(infoMap)
    }
}
