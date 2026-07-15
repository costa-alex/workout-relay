package io.github.costaalex.workoutrelay.infrastructure.sync

import io.github.costaalex.workoutrelay.app.workout.execution.SyncExecutionStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface SyncExecutionRepository :
    CrudRepository<SyncExecutionEntity, Int> {

    fun findAllByOrderByIdDesc(
        pageable: Pageable
    ): List<SyncExecutionEntity>

    fun findAllByStatus(
        status: SyncExecutionStatus
    ): List<SyncExecutionEntity>

    @Modifying(
        clearAutomatically = true,
        flushAutomatically = true
    )
    @Query(
        value = """
            DELETE FROM sync_executions
            WHERE id NOT IN (
                SELECT id
                FROM sync_executions
                ORDER BY id DESC
                LIMIT :retentionLimit
            )
        """,
        nativeQuery = true
    )
    fun deleteAllExceptLatest(
        @Param("retentionLimit")
        retentionLimit: Int
    ): Int
}