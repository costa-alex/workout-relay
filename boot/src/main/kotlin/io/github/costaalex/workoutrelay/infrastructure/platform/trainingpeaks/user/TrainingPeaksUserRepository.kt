package io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.user

import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository

@CacheConfig(cacheNames = ["tpUserCache"])
@Repository
class TrainingPeaksUserRepository(
    private val trainingPeaksUserApiClient: TrainingPeaksUserApiClient,
) {
    @Cacheable(key = "'singleton'")
    fun getUser(): TrainingPeaksUser {
        val dto =
            trainingPeaksUserApiClient.getUser()

        val account =
            dto.user.settings.account

        return TrainingPeaksUser(
            userId = dto.user.userId.toString(),
            isAthlete = account.isAthlete,
            isPremium = account.isPremium,
        )
    }
}
