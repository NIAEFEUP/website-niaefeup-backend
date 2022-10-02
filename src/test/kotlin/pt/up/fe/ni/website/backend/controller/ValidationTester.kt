package pt.up.fe.ni.website.backend.controller

import org.springframework.http.MediaType
import org.springframework.test.web.servlet.ResultActionsDsl

class ValidationTester(
    private val req: (Map<String, Any>) -> ResultActionsDsl,
    private val requiredFields: Map<String, Any> = mapOf()
) {
    lateinit var param: String

    fun isRequired() {
        val params = requiredFields.toMutableMap()
        params.remove(param)
        req(params)
            .expectValidationError()
            .andExpect {
                jsonPath("$.errors[0].message") { value("required") }
                jsonPath("$.errors[0].param") { value(param) }
            }
    }

    fun isNotEmpty() {
        val params = requiredFields.toMutableMap()
        params[param] = ""
        req(params)
            .expectValidationError()
            .andExpect {
                jsonPath("$.errors[0].message") { value("must not be empty") }
                jsonPath("$.errors[0].param") { value(param) }
                jsonPath("$.errors[0].value") { value("") }
            }
    }

    fun hasSizeBetween(min: Int, max: Int) {
        val params = requiredFields.toMutableMap()
        val smallValue = "a".repeat(min - 1)
        params[param] = smallValue
        req(params)
            .expectValidationError()
            .andExpect {
                jsonPath("$.errors[0].message") {
                    value("size must be between $min and $max")
                }
                jsonPath("$.errors[0].param") { value(param) }
                jsonPath("$.errors[0].value") { value(smallValue) }
            }

        val bigValue = "a".repeat(max + 1)
        params[param] = bigValue
        req(params)
            .expectValidationError()
            .andExpect {
                jsonPath("$.errors[0].message") {
                    value("size must be between $min and $max")
                }
                jsonPath("$.errors[0].param") { value(param) }
                jsonPath("$.errors[0].value") { value(bigValue) }
            }
    }

    fun hasMinSize(min: Int) {
        val params = requiredFields.toMutableMap()
        val smallValue = "a".repeat(min - 1)
        params[param] = smallValue
        req(params)
            .expectValidationError()
            .andExpect {
                jsonPath("$.errors[0].message") {
                    value("size must be greater or equal to $min")
                }
                jsonPath("$.errors[0].param") { value(param) }
                jsonPath("$.errors[0].value") { value(smallValue) }
            }
    }

    fun isDate() {
        val params = requiredFields.toMutableMap()
        params[param] = "invalid"
        req(params)
            .expectValidationError()
            .andExpect {
                jsonPath("$.errors[0].message") { value("must be date") }
                jsonPath("$.errors[0].value") { value("invalid") }
            }
    }

    private fun ResultActionsDsl.expectValidationError(): ResultActionsDsl {
        andExpect {
            status { isBadRequest() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.errors.length()") { value(1) }
        }
        return this
    }
}
