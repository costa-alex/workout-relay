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
    fun `should deserialize premium account information`() {
        val json = """
            {
              "user": {
                "userId": 3037421,
                "settings": {
                  "account": {
                    "userType": 4,
                    "isAthlete": true,
                    "isPremium": true,
                    "isCoached": false,
                    "premiumTrial": false
                  }
                }
              }
            }
        """.trimIndent()

        val dto =
            objectMapper.readValue<TrainingPeaksUserDTO>(
                json
            )

        val account =
            dto.user.settings.account

        assertEquals(
            3037421L,
            dto.user.userId,
        )

        assertTrue(account.isAthlete)
        assertTrue(account.isPremium)
        assertFalse(account.premiumTrial)
    }

    @Test
    fun `should deserialize basic account information`() {
        val json = """
            {
              "user": {
                "userId": 3037421,
                "settings": {
                  "account": {
                    "isAthlete": true,
                    "isPremium": false,
                    "premiumTrial": false
                  }
                }
              }
            }
        """.trimIndent()

        val dto =
            objectMapper.readValue<TrainingPeaksUserDTO>(
                json
            )

        assertTrue(
            dto.user.settings.account.isAthlete
        )

        assertFalse(
            dto.user.settings.account.isPremium
        )
    }
}