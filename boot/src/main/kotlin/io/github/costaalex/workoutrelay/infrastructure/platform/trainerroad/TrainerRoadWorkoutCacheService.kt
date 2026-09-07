package io.github.costaalex.workoutrelay.infrastructure.platform.trainerroad

import io.github.costaalex.workoutrelay.domain.workout.Workout
import io.github.costaalex.workoutrelay.infrastructure.platform.trainerroad.configuration.TrainerRoadConfigurationRepository
import io.github.costaalex.workoutrelay.infrastructure.platform.trainerroad.workout.TRWorkoutResponseDTO
import io.github.costaalex.workoutrelay.infrastructure.platform.trainerroad.workout.TrainerRoadWorkoutMapper
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper

@Service
@CacheConfig(cacheNames = ["trWorkoutCache"])
class TrainerRoadWorkoutCacheService(
    private val trainerRoadApiClient: TrainerRoadApiClient,
    private val trainerRoadConfigurationRepository: TrainerRoadConfigurationRepository,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @Cacheable
    fun getWorkout(trWorkoutId: String): Workout {
        val removeHtmlTags = trainerRoadConfigurationRepository.getConfiguration().removeHtmlTags
        return trainerRoadApiClient.getWorkout(trWorkoutId)
            .also(::logDiagnosticResponse)
            .let { TrainerRoadWorkoutMapper().toWorkout(it, removeHtmlTags) }
    }

    private fun logDiagnosticResponse(response: TRWorkoutResponseDTO) {
        val workout = response.workout
        runCatching {
            log.info(
                "[TEMP DIAGNOSTIC] TrainerRoad workout response id={}, name={}, duration={}, tss={}, intervalData={}, workoutData={}",
                workout.details.id,
                workout.details.workoutName,
                workout.details.duration,
                workout.details.tss,
                objectMapper.writeValueAsString(workout.intervalData),
                objectMapper.writeValueAsString(workout.workoutData),
            )
        }.onFailure {
            log.warn("Could not serialize temporary TrainerRoad diagnostic for workout {}", workout.details.id)
        }
    }
}