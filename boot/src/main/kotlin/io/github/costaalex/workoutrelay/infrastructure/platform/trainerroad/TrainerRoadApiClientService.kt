package io.github.costaalex.workoutrelay.infrastructure.platform.trainerroad

import io.github.costaalex.workoutrelay.domain.activity.Activity
import io.github.costaalex.workoutrelay.domain.workout.Workout
import io.github.costaalex.workoutrelay.domain.workout.WorkoutDetails
import io.github.costaalex.workoutrelay.domain.workout.structure.SingleStep
import io.github.costaalex.workoutrelay.infrastructure.platform.trainerroad.activity.TrainerRoadActivityDTO
import io.github.costaalex.workoutrelay.infrastructure.platform.trainerroad.activity.TrainerRoadActivityMapper
import io.github.costaalex.workoutrelay.infrastructure.platform.trainerroad.configuration.TrainerRoadConfigurationRepository
import io.github.costaalex.workoutrelay.infrastructure.platform.trainerroad.workout.TrainerRoadWorkoutMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class TrainerRoadApiClientService(
    private val trainerRoadApiClient: TrainerRoadApiClient,
    private val trainerRoadConfigurationRepository: TrainerRoadConfigurationRepository,
    private val trainerRoadWorkoutCacheService: TrainerRoadWorkoutCacheService,
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    fun findWorkoutsFromLibraryByName(name: String): List<WorkoutDetails> {
        val removeHtmlTags = trainerRoadConfigurationRepository.getConfiguration().removeHtmlTags
        return trainerRoadApiClient.findWorkouts(TRFindWorkoutsRequestDTO(name, 0, 500)).workouts
            .map { TrainerRoadWorkoutMapper().toWorkoutDetails(it, removeHtmlTags) }
    }

    private fun getTimeline(
        memberId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
    ): TrainerRoadTimelineDTO {
        require(!startDate.isAfter(endDate)) {
            "Start date cannot be after end date"
        }

        return trainerRoadApiClient.getTimeline(
            memberId,
            startDate.toString(),
            endDate.plusDays(1).toString(),
        )
    }

    fun getWorkoutsFromCalendar(startDate: LocalDate, endDate: LocalDate, memberId: Long): List<Workout> {
        return getTimeline(memberId, startDate, endDate)
            .plannedActivities
            .filter { it.date.toLocalDate() in startDate..endDate }
            .mapNotNull { plannedActivity ->
                plannedActivity.workoutId
                    ?.let { getWorkout(it.toString()) }
                    ?.withDate(plannedActivity.date.toLocalDate())
            }
    }

    fun getWorkout(trWorkoutId: String): Workout {
        return trainerRoadWorkoutCacheService.getWorkout(trWorkoutId)
            .also { workout ->
                log.debug(
                    "Mapped TrainerRoad workout {}, target preview: {}",
                    trWorkoutId,
                    targetPreview(workout),
                )
            }
    }

    fun getActivities(memberId: Long, startDate: LocalDate, endDate: LocalDate): List<Activity> {
        val activityIds = getTimeline(memberId, startDate, endDate)
            .activities
            .filter {
                val startedDate = it.started?.toLocalDate()
                startedDate != null && startedDate in startDate..endDate
            }
            .map { it.id }

        if (activityIds.isEmpty()) {
            return emptyList()
        }

        val activities = trainerRoadApiClient.getActivities(memberId, activityIds.joinToString(","))
            .filter { it.date.toLocalDate() in startDate..endDate }
        val activityMapper = TrainerRoadActivityMapper()
        return activities.mapNotNull { mapToActivity(activityMapper, it) }
    }

    private fun mapToActivity(activityMapper: TrainerRoadActivityMapper, it: TrainerRoadActivityDTO): Activity? {
        val activityId = it.completedRide?.WorkoutRecordId ?: it.activityId
        if (activityId == null) {
            log.warn("Skipping TrainerRoad activity {} because its export ID is missing", it.Id)
            return null
        }

        val resource = trainerRoadApiClient.exportFit(activityId.toString())
        return activityMapper.mapToActivity(it, resource)
    }

    private fun targetPreview(workout: Workout): String {
        return workout.structure?.steps
            ?.filterIsInstance<SingleStep>()
            ?.take(8)
            ?.joinToString { "${it.name}:${it.target.start}-${it.target.end}" }
            ?: "no structure"
    }
}