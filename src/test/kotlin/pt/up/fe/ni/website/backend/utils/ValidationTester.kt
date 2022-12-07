package pt.up.fe.ni.website.backend.utils

import org.springframework.http.MediaType
import org.springframework.test.web.servlet.ResultActionsDsl

class ValidationTester(
    private val req: (Map<String, Any?>) -> ResultActionsDsl,
    private val requiredFields: Map<String, Any?> = mapOf()
) {
    lateinit var param: String
    var parameterName: String? = null

    private fun getParamName(): String {
        return parameterName ?: param
    }

    fun isRequired() {
        val params = requiredFields.toMutableMap()
        params.remove(param)
        req(params)
            .andDo { print() }
            .expectValidationError()
            .andExpect {
                jsonPath("$.errors[0].message") { value("required") }
                jsonPath("$.errors[0].param") { value(getParamName()) }
            }
    }

    fun isNotEmpty() {
        val params = requiredFields.toMutableMap()
        params[param] = ""
        req(params)
            .expectValidationError()
            .andExpect {
                jsonPath("$.errors[0].message") { value("must not be empty") }
                jsonPath("$.errors[0].param") { value(getParamName()) }
                jsonPath("$.errors[0].value") { value("") }
            }
    }

    fun isNullOrNotBlank() {
        val params = requiredFields.toMutableMap()
        params[param] = ""
        req(params)
            .expectValidationError()
            .andExpect {
                jsonPath("$.errors[0].message") { value("must be null or not blank") }
                jsonPath("$.errors[0].param") { value(getParamName()) }
                jsonPath("$.errors[0].value") { value("") }
            }
    }

    fun isUrl() {
        val params = requiredFields.toMutableMap()
        params[param] = "invalid.com"
        req(params)
            .expectValidationError()
            .andExpect {
                jsonPath("$.errors[0].message") { value("must be a valid URL") }
                jsonPath("$.errors[0].param") { value(getParamName()) }
                jsonPath("$.errors[0].value") { value("invalid.com") }
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
                jsonPath("$.errors[0].param") { value(getParamName()) }
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
                jsonPath("$.errors[0].param") { value(getParamName()) }
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
                jsonPath("$.errors[0].param") { value(getParamName()) }
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

    fun isPastDate() {
        val params = requiredFields.toMutableMap()
        params[param] = "01-01-3000" // TODO: use a date in the future instead of hard coded
        req(params)
            .expectValidationError()
            .andExpect {
                jsonPath("$.errors[0].message") { value("must be a past date") }
                jsonPath("$.errors[0].value") { value("01-01-3000") }
            }
    }

    fun isAfter(otherDate: String) {
        val params = requiredFields.toMutableMap()
        params[param] = "01-01-2022"
        params[otherDate] = "02-01-2022"
        req(params)
            .expectValidationError()
            .andExpect {
                jsonPath("$.errors[0].message") { value("$param must be after $otherDate") }
            }
    }

    fun isEmail() {
        val params = requiredFields.toMutableMap()
        params[param] = "not-an-email"
        req(params)
            .expectValidationError()
            .andExpect {
                jsonPath("$.errors[0].message") { value("must be a well-formed email address") }
                jsonPath("$.errors[0].value") { value("not-an-email") }
                jsonPath("$.errors[0].param") { value(getParamName()) }
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
