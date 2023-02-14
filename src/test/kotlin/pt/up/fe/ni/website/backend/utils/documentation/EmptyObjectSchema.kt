package pt.up.fe.ni.website.backend.utils.documentation

import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation

class EmptyObjectSchema : PayloadSchema(
    "empty",
    listOf(
        PayloadDocumentation.fieldWithPath("").type(JsonFieldType.OBJECT).description("Empty object"),
    ),
)
