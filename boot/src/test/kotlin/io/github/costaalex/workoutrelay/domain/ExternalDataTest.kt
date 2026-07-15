package io.github.costaalex.workoutrelay.domain

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ExternalDataTest {
    @Test
    fun `should parse external data`() {
        // given
        val string = "//////////\nintervalsId=111\ntrainingPeaksId=222\ntrainerRoadId=333"

        // when
        var data = ExternalData(null, null, null)
        data = data.fromSimpleString(string)

        // then
        Assertions.assertNotNull(data)
        Assertions.assertEquals("111", data.intervalsId)
        Assertions.assertEquals("222", data.trainingPeaksId)
        Assertions.assertEquals("333", data.trainerRoadId)
    }

    @Test
    fun `should not parse data`() {
        // given
        val string = "//////////\nmyFavVar=111"
        val string1 = "//////\nmyFavVar=111"

        // when
        var data = ExternalData(null, null, null)
        data = data.fromSimpleString(string)
        var data1 = ExternalData(null, null, null)
        data1 = data1.fromSimpleString(string1)

        // then
        Assertions.assertNull(data.trainingPeaksId)
        Assertions.assertNull(data.intervalsId)
        Assertions.assertNull(data.trainerRoadId)
        Assertions.assertNull(data1.trainingPeaksId)
        Assertions.assertNull(data1.intervalsId)
        Assertions.assertNull(data1.trainerRoadId)
    }

    @Test
    fun `should be equal when all ids are equal`() {
        val first = ExternalData(
            trainingPeaksId = "tp-1",
            intervalsId = "intervals-1",
            trainerRoadId = "tr-1"
        )

        val second = ExternalData(
            trainingPeaksId = "tp-1",
            intervalsId = "intervals-1",
            trainerRoadId = "tr-1"
        )

        Assertions.assertEquals(first, second)
        Assertions.assertEquals(
            first.hashCode(),
            second.hashCode()
        )
    }

    @Test
    fun `should not be equal when only one id matches`() {
        val first = ExternalData(
            trainingPeaksId = "tp-1",
            intervalsId = null,
            trainerRoadId = "tr-1"
        )

        val second = ExternalData(
            trainingPeaksId = "tp-1",
            intervalsId = null,
            trainerRoadId = "tr-2"
        )

        Assertions.assertNotEquals(first, second)
    }

    @Test
    fun `should match when training peaks id is shared`() {
        val first = ExternalData(
            trainingPeaksId = "tp-1",
            intervalsId = "intervals-1",
            trainerRoadId = null
        )

        val second = ExternalData(
            trainingPeaksId = "tp-1",
            intervalsId = "intervals-2",
            trainerRoadId = null
        )

        Assertions.assertTrue(
            first.matchesAnyId(second)
        )
    }

    @Test
    fun `should match when trainer road id is shared`() {
        val first = ExternalData(
            trainingPeaksId = null,
            intervalsId = null,
            trainerRoadId = "tr-1"
        )

        val second = ExternalData(
            trainingPeaksId = "tp-2",
            intervalsId = null,
            trainerRoadId = "tr-1"
        )

        Assertions.assertTrue(
            first.matchesAnyId(second)
        )
    }

    @Test
    fun `should not match without a shared id`() {
        val first = ExternalData(
            trainingPeaksId = "tp-1",
            intervalsId = "intervals-1",
            trainerRoadId = "tr-1"
        )

        val second = ExternalData(
            trainingPeaksId = "tp-2",
            intervalsId = "intervals-2",
            trainerRoadId = "tr-2"
        )

        Assertions.assertFalse(
            first.matchesAnyId(second)
        )
    }

    @Test
    fun `should not match empty external data`() {
        val first = ExternalData.empty()
        val second = ExternalData.empty()

        Assertions.assertFalse(
            first.matchesAnyId(second)
        )
    }

    @Test
    fun `should not match blank ids`() {
        val first = ExternalData(
            trainingPeaksId = "",
            intervalsId = null,
            trainerRoadId = null
        )

        val second = ExternalData(
            trainingPeaksId = "",
            intervalsId = null,
            trainerRoadId = null
        )

        Assertions.assertFalse(
            first.matchesAnyId(second)
        )
    }
}
