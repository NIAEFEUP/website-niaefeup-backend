package pt.up.fe.ni.website.backend.utils.documentation.payloadschemas

import org.springframework.restdocs.payload.JsonFieldType
import pt.up.fe.ni.website.backend.utils.documentation.utils.DocumentedJSONField
import pt.up.fe.ni.website.backend.utils.documentation.utils.PayloadSchema

class ErrorSchema : PayloadSchema(
    "error",
    mutableListOf(
        DocumentedJSONField("errors[]", "Array of detected errors", JsonFieldType.ARRAY),
        DocumentedJSONField("errors[].message", "Error message of a given error", JsonFieldType.STRING),
        DocumentedJSONField(
            "errors[].param",
            "Parameter associated with the error",
            JsonFieldType.STRING,
            optional = true
        ),
        DocumentedJSONField("errors[].value", "Value that caused the error", JsonFieldType.VARIES, optional = true),
        DocumentedJSONField("errors[].value.*", optional = true, ignored = true)
    )
)
