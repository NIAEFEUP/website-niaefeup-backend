package pt.up.fe.ni.website.backend.utils.documentation.payloadschemas.model

import org.springframework.restdocs.payload.JsonFieldType
import pt.up.fe.ni.website.backend.utils.documentation.utils.DocumentedJSONField
import pt.up.fe.ni.website.backend.utils.documentation.utils.PayloadSchema

class PayloadDateInterval {
    companion object {
        val payload = PayloadSchema(
            "date-interval",
            mutableListOf(
                DocumentedJSONField(
                    "startDate",
                    "Event beginning date",
                    JsonFieldType.STRING
                ),
                DocumentedJSONField(
                    "endDate",
                    "Event finishing date",
                    JsonFieldType.STRING,
                    optional = true
                )
            )
        )
    }
}
