package io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.token

import io.github.costaalex.workoutrelay.domain.Platform
import io.github.costaalex.workoutrelay.infrastructure.PlatformException
import io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.configuration.TrainingPeaksConfigurationRepository
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository

@CacheConfig(cacheNames = ["tpAccessTokenCache"])
@Repository
class TrainingPeaksTokenRepository(
    private val trainingPeaksTokenApiClient: TrainingPeaksTokenApiClient,
    private val trainingPeaksConfigurationRepository: TrainingPeaksConfigurationRepository,
) {
    @Cacheable(key = "'singleton'")
    fun getToken(): String {
        val authCookie = trainingPeaksConfigurationRepository.getConfiguration().authCookie
            ?: throw PlatformException(Platform.TRAINING_PEAKS, "Access to the platform is not configured")
        val token = trainingPeaksTokenApiClient.getToken(authCookie)
        return token.accessToken!!
    }

}
