package io.github.costaalex.workoutrelay.domain.librarycontainer

import java.io.Serializable
import java.time.LocalDate
import io.github.costaalex.workoutrelay.domain.ExternalData
import io.github.costaalex.workoutrelay.infrastructure.utils.Date

data class LibraryContainer(
    val name: String,
    val startDate: LocalDate,
    val isPlan: Boolean,
    val workoutsAmount: Int,
    val externalData: ExternalData,
) : Serializable {
    companion object {
        fun planFromMonday(name: String, workoutsAmount: Int, externalData: ExternalData): LibraryContainer {
            return LibraryContainer(name, Date.thisMonday(), true, workoutsAmount, externalData)
        }
    }
}
