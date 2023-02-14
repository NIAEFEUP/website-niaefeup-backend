package pt.up.fe.ni.website.backend.utils.documentation

import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation

class ErrorSchema : PayloadSchema(
    "error",
    listOf(
        PayloadDocumentation.fieldWithPath("errors[]").type(JsonFieldType.ARRAY).description("Array of detected errors"),
        PayloadDocumentation.fieldWithPath("errors[].message").type(JsonFieldType.STRING).description("Error message of a given error"),
        PayloadDocumentation.fieldWithPath("errors[].param").type(JsonFieldType.STRING).description("Parameter associated with the error").optional(),
        PayloadDocumentation.fieldWithPath("errors[].value").type(JsonFieldType.VARIES).description("Value that caused the error").optional(),
        PayloadDocumentation.fieldWithPath("errors[].value.*").optional().ignored(),
    ),
)
