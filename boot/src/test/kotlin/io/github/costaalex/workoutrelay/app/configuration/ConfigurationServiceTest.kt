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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager

class ConfigurationServiceTest {

    private lateinit var platformConfigurationRepository:
        PlatformConfigurationRepository

    private lateinit var platformInfoRepository:
        PlatformInfoRepository

    private lateinit var appConfigurationRepository:
        AppConfigurationRepository

    private lateinit var debugModeService:
        DebugModeService

    private lateinit var cacheManager:
        CacheManager

    private lateinit var configurationService:
        ConfigurationService

    @BeforeEach
    fun setUp() {
        platformConfigurationRepository =
            mock(PlatformConfigurationRepository::class.java)

        platformInfoRepository =
            mock(PlatformInfoRepository::class.java)

        appConfigurationRepository =
            mock(AppConfigurationRepository::class.java)

        debugModeService =
            mock(DebugModeService::class.java)

        cacheManager =
            mock(CacheManager::class.java)

        `when`(platformConfigurationRepository.platform())
            .thenReturn(Platform.INTERVALS)

        `when`(platformInfoRepository.platform())
            .thenReturn(Platform.INTERVALS)

        configurationService =
            ConfigurationService(
                platformConfigurationRepositories =
                    listOf(platformConfigurationRepository),
                platformInfoRepositories =
                    listOf(platformInfoRepository),
                appConfigurationRepository =
                    appConfigurationRepository,
                debugModeService = debugModeService,
                cacheManager = cacheManager,
            )
    }

    @Test
    fun `should update platform configuration`() {
        val request =
            UpdateConfigurationRequest(
                mapOf(
                    "intervals.api-key" to "my-api-key",
                    "intervals.athlete-id" to "12345",
                )
            )

        val errors =
            configurationService.updateConfiguration(request)

        assertTrue(errors.isEmpty())

        verify(platformConfigurationRepository)
            .updateConfig(request)

        verify(debugModeService)
            .handleDebugMode(request.configMap)
    }

    @Test
    fun `should return platform error when update fails`() {
        val request =
            UpdateConfigurationRequest(
                mapOf(
                    "intervals.api-key" to "invalid-key",
                )
            )

        doThrow(
            PlatformException(
                Platform.INTERVALS,
                "Invalid credentials",
            )
        )
            .`when`(platformConfigurationRepository)
            .updateConfig(request)

        val errors =
            configurationService.updateConfiguration(request)

        assertEquals(
            listOf("Intervals.icu: Invalid credentials"),
            errors,
        )

        verify(debugModeService)
            .handleDebugMode(request.configMap)
    }

    @Test
    fun `should return stored configurations`() {
        val expected =
            AppConfiguration(
                mapOf(
                    "intervals.api-key" to "my-api-key",
                )
            )

        `when`(
            appConfigurationRepository.getConfigurations()
        ).thenReturn(expected)

        val result =
            configurationService.getConfigurations()

        assertSame(expected, result)
    }

    @Test
    fun `should return platform information`() {
        val expected =
            PlatformInfo(
                mapOf(
                    "isValid" to true,
                    "athleteId" to "12345",
                )
            )

        `when`(
            platformInfoRepository.platformInfo()
        ).thenReturn(expected)

        val result =
            configurationService.platformInfo()

        assertSame(
            expected,
            result[Platform.INTERVALS],
        )
    }

    @Test
    fun `should mark platform as invalid when validation fails`() {
        `when`(
            platformInfoRepository.platformInfo()
        ).thenThrow(
            RuntimeException("Unable to decode response")
        )

        val result =
            configurationService.platformInfo()

        assertEquals(
            false,
            result[Platform.INTERVALS]
                ?.infoMap
                ?.get("isValid"),
        )
    }

    @Test
    fun `should clear platform information cache`() {
        val cache =
            mock(Cache::class.java)

        val platformInfo =
            PlatformInfo(
                mapOf("isValid" to true)
            )

        `when`(
            cacheManager.getCache("platformInfoCache")
        ).thenReturn(cache)

        `when`(
            platformInfoRepository.platformInfo()
        ).thenReturn(platformInfo)

        configurationService.refreshPlatformInfo()

        verify(cache).clear()
    }
}