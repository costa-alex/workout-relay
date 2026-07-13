package org.freekode.tp2intervals.app.workout

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.freekode.tp2intervals.app.workout.schedule.C2CScheduledRequest
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import org.junit.jupiter.api.Test
import java.time.LocalDate

class C2CScheduledRequestTest {

    @Test
    fun `resolves offsets against the execution date`() {
        val request = C2CScheduledRequest(
            types = listOf(TrainingType.BIKE),
            skipSynced = true,
            sourcePlatform = Platform.TRAINER_ROAD,
            targetPlatform = Platform.TRAINING_PEAKS,
            startOffsetDays = -1,
            endOffsetDays = 2
        )

        val copyRequest = request.toCopyRequest(
            referenceDate = LocalDate.of(2026, 7, 13)
        )

        assertThat(copyRequest.startDate)
            .isEqualTo(LocalDate.of(2026, 7, 12))
        assertThat(copyRequest.endDate)
            .isEqualTo(LocalDate.of(2026, 7, 15))
        assertThat(copyRequest.replaceChangedWorkouts).isTrue()
    }

    @Test
    fun `defaults preserve current-day schedules`() {
        val request = C2CScheduledRequest(
            types = listOf(TrainingType.BIKE),
            skipSynced = true,
            sourcePlatform = Platform.INTERVALS,
            targetPlatform = Platform.TRAINING_PEAKS
        )

        val referenceDate = LocalDate.of(2026, 7, 13)
        val copyRequest = request.toCopyRequest(referenceDate)

        assertThat(copyRequest.startDate).isEqualTo(referenceDate)
        assertThat(copyRequest.endDate).isEqualTo(referenceDate)
        assertThat(copyRequest.replaceChangedWorkouts).isFalse()
    }

    @Test
    fun `rejects a start offset after the end offset`() {
        assertThatIllegalArgumentException().isThrownBy {
            C2CScheduledRequest(
                types = listOf(TrainingType.BIKE),
                skipSynced = true,
                sourcePlatform = Platform.INTERVALS,
                targetPlatform = Platform.TRAINING_PEAKS,
                startOffsetDays = 2,
                endOffsetDays = 1
            )
        }
    }
}
