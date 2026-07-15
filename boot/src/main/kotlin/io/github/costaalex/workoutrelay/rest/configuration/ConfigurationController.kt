package io.github.costaalex.workoutrelay.rest.configuration

import io.github.costaalex.workoutrelay.app.configuration.ConfigurationService
import io.github.costaalex.workoutrelay.domain.Platform
import io.github.costaalex.workoutrelay.domain.config.UpdateConfigurationRequest
import io.github.costaalex.workoutrelay.domain.workout.structure.StepModifier
import io.github.costaalex.workoutrelay.rest.ErrorResponseDTO
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PostMapping

@RestController
class ConfigurationController(
    private val configurationService: ConfigurationService,
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @GetMapping("/api/configuration")
    fun getConfigurations(): AppConfigurationDTO {
        log.debug("Received request for getting all configurations")
        val configurations = configurationService.getConfigurations()
        return AppConfigurationDTO(configurations.configMap)
    }

    @PutMapping("/api/configuration")
    fun updateConfiguration(@RequestBody requestDTO: UpdateConfigurationRequestDTO): ResponseEntity<ErrorResponseDTO> {
        log.debug("Received request for updating configuration")
        val errors = configurationService.updateConfiguration(UpdateConfigurationRequest(requestDTO.config))
        if (errors.isNotEmpty()) {
            return ResponseEntity.badRequest().body(ErrorResponseDTO(errors.joinToString()))
        }
        return ResponseEntity.ok().build()
    }

    @GetMapping("/api/configuration/intervals-step-modifiers")
    fun getIntervalsStepModifiers(): List<StepModifier> {
        return StepModifier.entries
    }

    @GetMapping("/api/configuration/platform")
    fun getAllPlatformInfo() =
        configurationService.platformInfo()

    @GetMapping("/api/configuration/{platform}")
    fun getConfigurations(@PathVariable platform: Platform) =
        configurationService.platformInfo(platform)
        
    @PostMapping("/api/configuration/platform/refresh")
    fun refreshAllPlatformInfo() =
        configurationService.refreshPlatformInfo()
}
