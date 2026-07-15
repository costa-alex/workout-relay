package io.github.costaalex.workoutrelay.domain.librarycontainer

import io.github.costaalex.workoutrelay.domain.Platform
import java.time.LocalDate
import io.github.costaalex.workoutrelay.domain.ExternalData

interface LibraryContainerRepository {
    fun platform(): Platform

    fun createLibraryContainer(name: String, isPlan: Boolean, startDate: LocalDate?): LibraryContainer

    fun getLibraryContainers(): List<LibraryContainer>

    fun deleteLibraryContainer(externalData: ExternalData)
}
