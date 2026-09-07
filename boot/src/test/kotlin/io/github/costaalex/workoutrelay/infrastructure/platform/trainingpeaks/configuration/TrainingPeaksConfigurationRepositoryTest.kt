package io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.configuration

import io.github.costaalex.workoutrelay.domain.config.AppConfiguration
import io.github.costaalex.workoutrelay.domain.config.AppConfigurationRepository
import io.github.costaalex.workoutrelay.domain.config.UpdateConfigurationRequest
import io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.token.TrainingPeaksTokenApiClient
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager

class TrainingPeaksConfigurationRepositoryTest {
    @Test
    fun `clears account caches after updating configuration`() {
        val appConfigurationRepository = mock<AppConfigurationRepository>()
        val cacheManager = mock<CacheManager>()
        whenever(cacheManager.getCache("platformInfoCache"))
            .thenReturn(mock<Cache>())
        val accountCaches = listOf(
            "tpAccessTokenCache",
            "tpUserCache",
            "tpWorkoutsCache",
            "libraryItemsCache",
        ).associateWith { mock<Cache>() }
        accountCaches.forEach { (name, cache) ->
            whenever(cacheManager.getCache(name)).thenReturn(cache)
        }
        whenever(appConfigurationRepository.getConfigurationByPrefix("training-peaks"))
            .thenReturn(
                AppConfiguration(
                    mapOf("training-peaks.copy-plan-days-shift" to "0")
                )
            )
        val repository = TrainingPeaksConfigurationRepository(
            appConfigurationRepository,
            mock<TrainingPeaksTokenApiClient>(),
            cacheManager,
        )
        val request = UpdateConfigurationRequest(
            mapOf("training-peaks.auth-cookie" to null),
        )

        repository.updateConfig(request)

        verify(appConfigurationRepository).updateConfig(
            UpdateConfigurationRequest(
                mapOf(
                    "training-peaks.copy-plan-days-shift" to "0",
                    "training-peaks.auth-cookie" to null,
                )
            )
        )
        accountCaches.values.forEach { cache ->
            verify(cache).clear()
        }
    }
}