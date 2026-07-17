package io.github.costaalex.workoutrelay.infrastructure.platform.trainingpeaks.user

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue

class TrainingPeaksUserDTOTest {

    private val objectMapper =
        jacksonObjectMapper()

    @Test
    fun `should default isPremium to false when missing`() {
        val json = """
            {
              "user": {
                "userId": 12345
              },
              "accountStatus": {
                "isAthlete": true
              }
            }
        """.trimIndent()

        val user =
            objectMapper.readValue<TrainingPeaksUserDTO>(
                json
            )

        assertEquals("12345", user.userId)
        assertTrue(user.accountStatus.isAthlete)
        assertFalse(user.accountStatus.isPremium)
    }

    @Test
    fun `should deserialize isPremium when present`() {
        val json = """
            {
              "user": {
                "userId": 12345
              },
              "accountStatus": {
                "isAthlete": true,
                "isPremium": true
              }
            }
        """.trimIndent()

        val user =
            objectMapper.readValue<TrainingPeaksUserDTO>(
                json
            )

        assertEquals("12345", user.userId)
        assertTrue(user.accountStatus.isAthlete)
        assertTrue(user.accountStatus.isPremium)
    }
}