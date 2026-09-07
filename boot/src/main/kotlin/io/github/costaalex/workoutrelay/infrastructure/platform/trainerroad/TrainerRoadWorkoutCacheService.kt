package io.github.costaalex.workoutrelay.infrastructure.platform.trainerroad

import io.github.costaalex.workoutrelay.domain.workout.Workout
import io.github.costaalex.workoutrelay.infrastructure.platform.trainerroad.configuration.TrainerRoadConfigurationRepository
import io.github.costaalex.workoutrelay.infrastructure.platform.trainerroad.workout.TrainerRoadWorkoutMapper
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
@CacheConfig(cacheNames = ["trWorkoutCache"])
class TrainerRoadWorkoutCacheService(
    private val trainerRoadApiClient: TrainerRoadApiClient,
    private val trainerRoadConfigurationRepository: TrainerRoadConfigurationRepository,
) {
    @Cacheable
    fun getWorkout(trWorkoutId: String): Workout {
        val removeHtmlTags = trainerRoadConfigurationRepository.getConfiguration().removeHtmlTags
        return trainerRoadApiClient.getWorkout(trWorkoutId)
            .let { TrainerRoadWorkoutMapper().toWorkout(it, removeHtmlTags) }
    }
}