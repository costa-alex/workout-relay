package config.mock

import com.fasterxml.jackson.annotation.JsonInclude
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.SerializationFeature
import tools.jackson.module.kotlin.jacksonMapperBuilder

object ObjectMapperFactory {

    fun objectMapper(): ObjectMapper =
        jacksonMapperBuilder()
            .changeDefaultPropertyInclusion { inclusion ->
                inclusion.withValueInclusion(
                    JsonInclude.Include.NON_NULL
                )
            }
            .disable(
                SerializationFeature.FAIL_ON_EMPTY_BEANS
            )
            .disable(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
            )
            .build()
}