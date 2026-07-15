package io.github.costaalex.workoutrelay.domain.config

import io.github.costaalex.workoutrelay.domain.Platform
import org.springframework.stereotype.Repository

@Repository
class GenericPlatformConfigurationRepository(
    private val appConfigurationRepository: AppConfigurationRepository,

) : PlatformConfigurationRepository {
    override fun platform() = Platform.GENERIC

    override fun updateConfig(request: UpdateConfigurationRequest) {
        appConfigurationRepository.updateConfig(UpdateConfigurationRequest(request.getByPrefix(Platform.GENERIC.key)))
    }
}
