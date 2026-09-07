package io.github.costaalex.workoutrelay.infrastructure.schedule

import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.sql.DriverManager

class ScheduleMigrationTest {
    @TempDir
    lateinit var temporaryDirectory: Path

    @Test
    fun `preserves schedules when upgrading through migration 0008`() {
        val databaseUrl = "jdbc:sqlite:${temporaryDirectory.resolve("upgrade.sqlite")}" 
        migrate(databaseUrl, "db/changelog/before-0008.yaml")

        val requestJson = """{"types":["BIKE"],"skipSynced":true}"""
        DriverManager.getConnection(databaseUrl).use { connection ->
            connection.prepareStatement(
                "INSERT INTO schedule_requests (id, request_json) VALUES (?, ?)"
            ).use { statement ->
                statement.setInt(1, 42)
                statement.setString(2, requestJson)
                statement.executeUpdate()
            }
        }

        migrate(databaseUrl, "db/changelog/db.changelog-master.yaml")

        DriverManager.getConnection(databaseUrl).use { connection ->
            connection.prepareStatement(
                "SELECT id, request_json FROM schedule_requests"
            ).use { statement ->
                statement.executeQuery().use { result ->
                    assertThat(result.next()).isTrue()
                    assertThat(result.getInt("id")).isEqualTo(42)
                    assertThat(result.getString("request_json")).isEqualTo(requestJson)
                    assertThat(result.next()).isFalse()
                }
            }
        }
    }

    private fun migrate(databaseUrl: String, changeLog: String) {
        DriverManager.getConnection(databaseUrl).use { connection ->
            val database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(JdbcConnection(connection))
            Liquibase(
                changeLog,
                ClassLoaderResourceAccessor(),
                database,
            ).update()
        }
    }
}