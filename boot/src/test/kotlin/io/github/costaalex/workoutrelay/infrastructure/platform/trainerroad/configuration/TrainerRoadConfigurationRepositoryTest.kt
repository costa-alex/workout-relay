package io.github.costaalex.workoutrelay.infrastructure.platform.trainerroad.configuration

import io.github.costaalex.workoutrelay.domain.config.AppConfiguration
import io.github.costaalex.workoutrelay.domain.config.AppConfigurationRepository
import io.github.costaalex.workoutrelay.domain.config.UpdateConfigurationRequest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager

class TrainerRoadConfigurationRepositoryTest {
    @Test
    fun `should clear account caches after updating configuration`() {
        val appConfigurationRepository = mock<AppConfigurationRepository>()
        val cacheManager = mock<CacheManager>()
        val platformInfoCache = mock<Cache>()
        whenever(cacheManager.getCache("platformInfoCache")).thenReturn(platformInfoCache)
        val accountCaches = listOf(
            "trCookieCache",
            "trUsernameCache",
            "trMemberIdCache",
            "trWorkoutCache",
        ).associateWith { mock<Cache>() }
        accountCaches.forEach { (name, cache) ->
            whenever(cacheManager.getCache(name)).thenReturn(cache)
        }
        whenever(appConfigurationRepository.getConfigurationByPrefix("trainer-road"))
            .thenReturn(AppConfiguration(emptyMap()))
        val repository = TrainerRoadConfigurationRepository(
            appConfigurationRepository,
            mock<TrainerRoadValidationApiClient>(),
            cacheManager,
        )
        val request = UpdateConfigurationRequest(
            mapOf("trainer-road.remove-html-tags" to "true"),
        )

        repository.updateConfig(request)

        verify(appConfigurationRepository).updateConfig(request)
        accountCaches.values.forEach { cache ->
            verify(cache).clear()
        }
    }
}