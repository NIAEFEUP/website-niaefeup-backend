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
                DocumentedJSONField(
                    "hallOfFame",
                    "Array of members that were once associated with the project",
                    JsonFieldType.ARRAY
                ),
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
                    "image",
                    "Path to the image",
                    JsonFieldType.STRING
                ),
                DocumentedJSONField(
                    "targetAudience",
                    "Information about the target audience",
                    JsonFieldType.STRING
                ),
                DocumentedJSONField(
                    "slogan",
                    "Slogan of the activity",
                    JsonFieldType.STRING
                ),
                DocumentedJSONField(
                    "github",
                    "Handle/link to the activity's GitHub repository",
                    JsonFieldType.STRING
                ),
                DocumentedJSONField(
                    "links",
                    "Array of links associated with the activity",
                    JsonFieldType.ARRAY,
                    optional = true
                ),
                DocumentedJSONField("links[].url", "URL to the link", JsonFieldType.STRING, optional = true),
                DocumentedJSONField(
                    "links[].iconPath",
                    "URL to the link's icon",
                    JsonFieldType.STRING,
                    optional = true
                ),
                DocumentedJSONField(
                    "links[].label",
                    "Label for the link",
                    JsonFieldType.STRING,
                    optional = true
                ),
                DocumentedJSONField(
                    "timeline",
                    "Array of events defining the activity's timeline",
                    JsonFieldType.ARRAY,
                    optional = true
                ),
                DocumentedJSONField(
                    "timeline[].date",
                    "Date of the event",
                    JsonFieldType.STRING,
                    optional = true
                ),
                DocumentedJSONField(
                    "timeline[].description",
                    "Description of the event",
                    JsonFieldType.STRING,
                    optional = true
                )
            ).addFieldsBeneathPath(
                "dateInterval",
                PayloadDateInterval.payload.documentedJSONFields,
                addRequest = true,
                addResponse = true
            ).addFieldsBeneathPath(
                "teamMembers[]",
                PayloadAccount().payload.documentedJSONFields,
                addRequest = true,
                addResponse = true,
                optional = true
            )
        )
    }
}
