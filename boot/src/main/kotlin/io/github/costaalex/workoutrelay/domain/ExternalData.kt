package io.github.costaalex.workoutrelay.domain

import java.io.Serializable

data class ExternalData(
    val trainingPeaksId: String?,
    val intervalsId: String?,
    val trainerRoadId: String?,
) : Serializable {
    
    companion object {
        const val DESCRIPTION_SEPARATOR = "//////////"
         
        fun empty() = ExternalData(null, null, null)
    }

    fun withTrainingPeaks(trainingPeaksId: String) = ExternalData(trainingPeaksId, intervalsId, trainerRoadId)

    fun withIntervals(intervalsId: String) = ExternalData(trainingPeaksId, intervalsId, trainerRoadId)

    fun withTrainerRoad(trainerRoadId: String) = ExternalData(trainingPeaksId, intervalsId, trainerRoadId)

    fun fromSimpleString(string: String): ExternalData {
        val normalized = string
            .replace("<br\\s*/?>".toRegex(RegexOption.IGNORE_CASE), "\n")
            .replace("</p>".toRegex(RegexOption.IGNORE_CASE), "\n")
            .replace("<[^>]*>".toRegex(), "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")

        val metadata = if (normalized.contains(DESCRIPTION_SEPARATOR)) {
            normalized.substringAfter(DESCRIPTION_SEPARATOR)
        } else {
            normalized
        }

        var externalData = this

        fun readField(name: String): String? {
            return metadata
                .lineSequence()
                .map { it.trim() }
                .firstOrNull { it.startsWith("$name=") }
                ?.substringAfter("=")
                ?.trim()
                ?.takeIf { it.isNotBlank() }
        }

        if (externalData.trainingPeaksId == null) {
            readField("trainingPeaksId")?.let {
                externalData = externalData.withTrainingPeaks(it)
            }
        }

        if (externalData.intervalsId == null) {
            readField("intervalsId")?.let {
                externalData = externalData.withIntervals(it)
            }
        }

        if (externalData.trainerRoadId == null) {
            readField("trainerRoadId")?.let {
                externalData = externalData.withTrainerRoad(it)
            }
        }

        return externalData
    }

    fun toSimpleString(): String {
        val outList = mutableListOf<String>()
        if (trainingPeaksId != null) outList.add("trainingPeaksId=$trainingPeaksId")
        if (intervalsId != null) outList.add("intervalsId=$intervalsId")
        if (trainerRoadId != null) outList.add("trainerRoadId=$trainerRoadId")
        val simpleString = outList.joinToString(separator = "\n")
        return """
                $DESCRIPTION_SEPARATOR
                $simpleString
            """.trimIndent()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExternalData

        if (trainingPeaksId != null && trainingPeaksId == other.trainingPeaksId) return true
        if (intervalsId != null && intervalsId == other.intervalsId) return true
        if (trainerRoadId != null && trainerRoadId == other.trainerRoadId) return true

        return false
    }

    override fun hashCode(): Int {
        var result = trainingPeaksId?.hashCode() ?: 0
        result = 31 * result + (intervalsId?.hashCode() ?: 0)
        result = 31 * result + (trainerRoadId?.hashCode() ?: 0)
        return result
    }


}
