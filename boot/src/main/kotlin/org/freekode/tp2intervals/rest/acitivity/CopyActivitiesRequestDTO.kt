package org.freekode.tp2intervals.rest.activity

import org.freekode.tp2intervals.domain.Platform
import org.freekode.tp2intervals.domain.TrainingType

class CopyActivitiesRequestDTO(
    val startDate: String,
    val endDate: String,
    val types: List<TrainingType>,
    val sourcePlatform: Platform,
    val targetPlatform: Platform
)
