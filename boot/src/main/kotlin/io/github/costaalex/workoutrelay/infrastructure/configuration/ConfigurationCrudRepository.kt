package io.github.costaalex.workoutrelay.infrastructure.configuration

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ConfigurationCrudRepository : CrudRepository<AppConfigurationEntryEntity, String> {

    fun findByKeyLike(prefix: String): List<AppConfigurationEntryEntity>
}
