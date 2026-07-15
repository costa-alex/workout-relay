package io.github.costaalex.workoutrelay

import io.github.costaalex.workoutrelay.infrastructure.configuration.DefaultConfiguration
import io.github.costaalex.workoutrelay.infrastructure.configuration.SchedulerProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.scheduling.annotation.EnableScheduling
import io.github.costaalex.workoutrelay.infrastructure.configuration.SyncHistoryProperties

@SpringBootApplication
@EnableFeignClients
@EnableCaching
@EnableScheduling
@EnableConfigurationProperties(
    DefaultConfiguration::class,
    SchedulerProperties::class,
    SyncHistoryProperties::class
)
class WorkoutRelayApplication

fun main(args: Array<String>) {
    runApplication<WorkoutRelayApplication>(*args)
}
