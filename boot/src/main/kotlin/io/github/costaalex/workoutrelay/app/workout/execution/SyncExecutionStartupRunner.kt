package io.github.costaalex.workoutrelay.app.workout.execution

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class SyncExecutionStartupRunner(
    private val maintenanceService:
        SyncExecutionMaintenanceService
) : ApplicationRunner {

    private val log =
        LoggerFactory.getLogger(this.javaClass)

    override fun run(args: ApplicationArguments) {
        val interrupted =
            maintenanceService
                .recoverInterruptedExecutions()

        val deleted =
            maintenanceService.enforceRetention()

        log.info(
            "Sync history startup maintenance completed. " +
                "interrupted={}, deleted={}",
            interrupted,
            deleted
        )
    }
}