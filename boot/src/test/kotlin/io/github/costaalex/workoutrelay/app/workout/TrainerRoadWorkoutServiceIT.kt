package io.github.costaalex.workoutrelay.app.workout

import config.BaseSpringITConfig
import io.github.costaalex.workoutrelay.app.plan.CreateLibraryContainerRequest
import io.github.costaalex.workoutrelay.app.plan.DeleteLibraryRequest
import io.github.costaalex.workoutrelay.app.plan.LibraryService
import io.github.costaalex.workoutrelay.domain.Platform
import io.github.costaalex.workoutrelay.domain.TrainingType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.time.LocalDateTime


class TrainerRoadWorkoutServiceIT : BaseSpringITConfig() {
    @Autowired
    lateinit var libraryService: LibraryService

    @Autowired
    lateinit var workoutService: WorkoutService

    private val platform = Platform.TRAINER_ROAD

    @Test
    fun `should copy workouts from library to library`() {
        val foundWorkouts = workoutService.findWorkoutsByName(platform, "complex")

        val libraryContainer = libraryService.create(
            CreateLibraryContainerRequest("copy form lib to lib ${LocalDateTime.now()}", Platform.INTERVALS)
        )

        val copyRequest = CopyFromLibraryToLibraryRequest(
            foundWorkouts.first().externalData,
            libraryContainer,
            platform,
            Platform.INTERVALS
        )
        val response = workoutService.copyWorkoutL2L(copyRequest)
        libraryService.deleteLibrary(DeleteLibraryRequest(libraryContainer.externalData, Platform.INTERVALS))
        assertEquals(response.copied, 1)
    }

    @Test
    @Disabled("don't have example response for calendar")
    fun `should copy planned workouts to library`() {
        val response = workoutService.copyWorkoutsC2L(
            CopyFromCalendarToLibraryRequest(
                LocalDate.parse("2024-03-04"),
                LocalDate.parse("2024-03-10"),
                "copy from calend to lib ${LocalDateTime.now()}",
                true,
                TrainingType.Companion.DEFAULT_LIST,
                platform,
                Platform.INTERVALS
            )
        )
        libraryService.deleteLibrary(DeleteLibraryRequest(response.externalData, Platform.INTERVALS))

        Assertions.assertEquals(response.copied, 5)
    }
}
