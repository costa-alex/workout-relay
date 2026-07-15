package io.github.costaalex.workoutrelay.infrastructure.schedule

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ScheduleRequestRepository :
    CrudRepository<ScheduleRequestEntity, Int>
