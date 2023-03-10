package pt.up.fe.ni.website.backend.utils.documentation.payloadschemas.model

import org.springframework.restdocs.payload.JsonFieldType
import pt.up.fe.ni.website.backend.utils.documentation.utils.DocumentedJSONField
import pt.up.fe.ni.website.backend.utils.documentation.utils.DocumentedJSONField.Companion.addFieldsBeneathPath
import pt.up.fe.ni.website.backend.utils.documentation.utils.PayloadSchema

class PayloadActivity {
    companion object {
        val payload = PayloadSchema(
            "activity",
            mutableListOf(
                DocumentedJSONField("id", "Id of the activity", JsonFieldType.NUMBER),
                DocumentedJSONField("title", "Title of the activity", JsonFieldType.STRING),
                DocumentedJSONField("description", "Description of the activity", JsonFieldType.STRING),
                DocumentedJSONField("teamMembers", "Array of team members", JsonFieldType.ARRAY),
                DocumentedJSONField("isArchived", "If the activity is archived", JsonFieldType.BOOLEAN),
                DocumentedJSONField(
                    "technologies",
                    "Array of technologies",
                    JsonFieldType.ARRAY,
                    optional = true
                ),
                DocumentedJSONField("technologies[].*", "Technology", JsonFieldType.STRING, optional = true),
                DocumentedJSONField(
                    "dateInterval",
                    "Date interval of the activity",
                    JsonFieldType.OBJECT,
                    optional = true
                ),
                DocumentedJSONField(
                    "thumbnailPath",
                    "Path to the thumbnail",
                    JsonFieldType.STRING,
                    optional = true
                )
            ).addFieldsBeneathPath(
                "",
                PayloadDateInterval.payload.documentedJSONFields,
                addRequest = true,
                addResponse = true
            )
        )
    }
}
