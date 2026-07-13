package org.freekode.tp2intervals.app.workout.schedule

import org.freekode.tp2intervals.app.workout.CopyFromCalendarToCalendarRequest
import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType
import java.time.LocalDate

data class C2CScheduledRequest(
    val types: List<TrainingType>,
    val skipSynced: Boolean,
    val sourcePlatform: Platform,
    val targetPlatform: Platform,
    val startOffsetDays: Int = 0,
    val endOffsetDays: Int = 0
) : Schedulable {

    init {
        require(startOffsetDays in MIN_OFFSET_DAYS..MAX_OFFSET_DAYS) {
            "Start offset must be between $MIN_OFFSET_DAYS and $MAX_OFFSET_DAYS days"
        }

        require(endOffsetDays in MIN_OFFSET_DAYS..MAX_OFFSET_DAYS) {
            "End offset must be between $MIN_OFFSET_DAYS and $MAX_OFFSET_DAYS days"
        }

        require(startOffsetDays <= endOffsetDays) {
            "Start offset cannot be after end offset"
        }
    }

    fun toCopyRequest(
        referenceDate: LocalDate = LocalDate.now()
    ) = CopyFromCalendarToCalendarRequest(
        startDate = referenceDate.plusDays(startOffsetDays.toLong()),
        endDate = referenceDate.plusDays(endOffsetDays.toLong()),
        types = types,
        skipSynced = skipSynced,
        sourcePlatform = sourcePlatform,
        targetPlatform = targetPlatform,
        replaceChangedWorkouts =
            sourcePlatform == Platform.TRAINER_ROAD &&
                targetPlatform == Platform.TRAINING_PEAKS
    )

    companion object {
        const val MIN_OFFSET_DAYS = -365
        const val MAX_OFFSET_DAYS = 365
    }
}
