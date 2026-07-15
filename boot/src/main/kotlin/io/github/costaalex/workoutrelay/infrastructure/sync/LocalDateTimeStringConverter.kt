package io.github.costaalex.workoutrelay.infrastructure.sync

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField

@Converter(autoApply = false)
class LocalDateTimeStringConverter :
    AttributeConverter<LocalDateTime, String> {

    companion object {
        private val WRITE_FORMATTER =
            DateTimeFormatter.ofPattern(
                "yyyy-MM-dd HH:mm:ss.SSS"
            )

        private val READ_FORMATTER =
            DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd HH:mm:ss")
                .optionalStart()
                .appendFraction(
                    ChronoField.NANO_OF_SECOND,
                    1,
                    9,
                    true
                )
                .optionalEnd()
                .toFormatter()
    }

    override fun convertToDatabaseColumn(
        attribute: LocalDateTime?
    ): String? {
        return attribute?.format(WRITE_FORMATTER)
    }

    override fun convertToEntityAttribute(
        dbData: String?
    ): LocalDateTime? {
        val value = dbData
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: return null

        /*
         * Compatibilidade com os registos criados antes
         * desta correção, guardados como epoch milliseconds.
         */
        value.toLongOrNull()?.let { epochMillis ->
            return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(epochMillis),
                ZoneId.systemDefault()
            )
        }

        try {
            return LocalDateTime.parse(
                value,
                READ_FORMATTER
            )
        } catch (_: DateTimeParseException) {
            // Pode existir algum valor no formato ISO com "T".
        }

        try {
            return LocalDateTime.parse(
                value,
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
            )
        } catch (exception: DateTimeParseException) {
            throw IllegalArgumentException(
                "Unsupported database date-time value: $value",
                exception
            )
        }
    }
}