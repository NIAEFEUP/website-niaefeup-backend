package pt.up.fe.ni.website.backend.controller

import org.hamcrest.Matchers.greaterThan
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
            .andExpect {
                status { isBadRequest() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.errors.length()") { value(1) }
                jsonPath("$.errors[0].message") { value("required") }
                jsonPath("$.errors[0].param") { value(param) }
            }
    }

    fun hasSizeBetween(min: Int, max: Int) {
        val params = requiredFields.toMutableMap()
        params[param] = "a".repeat(min - 1)
        req(params)
            .andExpect {
                status { isBadRequest() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.errors.length()") { value(greaterThan(0)) }
                jsonPath("$.errors[0].message") {
                    value("size must be between $min and $max")
                }
                jsonPath("$.errors[0].param") { value(param) }
            }

        params[param] = "a".repeat(max + 1)
        req(params)
            .andExpect {
                status { isBadRequest() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.errors.length()") { value(greaterThan(0)) }
                jsonPath("$.errors[0].message") {
                    value("size must be between $min and $max")
                }
                jsonPath("$.errors[0].param") { value(param) }
            }
    }
}
