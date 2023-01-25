package pt.up.fe.ni.website.backend.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException

fun isSerializable(value: Any): Boolean {
    return try {
        val objectMapper = ObjectMapper()
        objectMapper.writeValueAsString(value)
        true
    } catch (err: InvalidDefinitionException) {
        System.err.println(err)
        false
    }
}
