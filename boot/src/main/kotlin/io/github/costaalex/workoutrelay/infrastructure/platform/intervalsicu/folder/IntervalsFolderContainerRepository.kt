package io.github.costaalex.workoutrelay.infrastructure.platform.intervalsicu.folder

import java.time.LocalDate
import io.github.costaalex.workoutrelay.domain.ExternalData
import io.github.costaalex.workoutrelay.domain.Platform
import io.github.costaalex.workoutrelay.domain.librarycontainer.LibraryContainer
import io.github.costaalex.workoutrelay.domain.librarycontainer.LibraryContainerRepository
import io.github.costaalex.workoutrelay.infrastructure.Signature
import io.github.costaalex.workoutrelay.infrastructure.platform.intervalsicu.configuration.IntervalsConfigurationRepository
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Repository


@CacheConfig(cacheNames = ["libraryItemsCache"])
@Repository
class IntervalsFolderContainerRepository(
    private val intervalsFolderApiClient: IntervalsFolderApiClient,
    private val intervalsConfigurationRepository: IntervalsConfigurationRepository,
) : LibraryContainerRepository {

    override fun platform() = Platform.INTERVALS

    override fun createLibraryContainer(name: String, isPlan: Boolean, startDate: LocalDate?): LibraryContainer {
        val folderType = if (isPlan) "PLAN" else "FOLDER"
        val newFolder = createFolder(name, startDate, folderType)
        return toPlan(newFolder)
    }

    @Cacheable(key = "'INTERVALS'")
    override fun getLibraryContainers(): List<LibraryContainer> {
        return intervalsFolderApiClient.getFolders(intervalsConfigurationRepository.getConfiguration().athleteId)
            .map { toPlan(it) }
    }

    override fun deleteLibraryContainer(externalData: ExternalData) {
        intervalsFolderApiClient.deleteFolder(
            intervalsConfigurationRepository.getConfiguration().athleteId,
            externalData.intervalsId!!
        )
    }

    private fun createFolder(name: String, startDate: LocalDate?, type: String): FolderDTO {
        val createRequest = CreateFolderRequestDTO(
            0, name, Signature.description, 0, startDate?.toString(), -1, -1, type
        )
        return intervalsFolderApiClient.createFolder(
            intervalsConfigurationRepository.getConfiguration().athleteId,
            createRequest
        )
    }

    private fun toPlan(folderDTO: FolderDTO): LibraryContainer {
        return if (folderDTO.type == "PLAN") {
            LibraryContainer(
                folderDTO.name,
                folderDTO.startDateLocal!!,
                true,
                folderDTO.num_workouts,
                ExternalData.empty().withIntervals(folderDTO.id)
            )
        } else {
            LibraryContainer.planFromMonday(
                folderDTO.name,
                folderDTO.num_workouts,
                ExternalData.empty().withIntervals(folderDTO.id)
            )
        }
    }
}
