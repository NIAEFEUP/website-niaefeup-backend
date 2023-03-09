package pt.up.fe.ni.website.backend.documentation.payloadschemas.model

import org.springframework.restdocs.payload.JsonFieldType
import pt.up.fe.ni.website.backend.utils.documentation.DocumentedJSONField
import pt.up.fe.ni.website.backend.utils.documentation.DocumentedJSONField.Companion.addFieldsBeneathPath
import pt.up.fe.ni.website.backend.utils.documentation.ModelDocumentation

class PayloadEvent : ModelDocumentation(
    Companion.Tag.EVENT.name.lowercase(),
    Companion.Tag.EVENT,
    mutableListOf(
        DocumentedJSONField("title", "Event title", JsonFieldType.STRING),
        DocumentedJSONField("description", "Event description", JsonFieldType.STRING),
        DocumentedJSONField("thumbnailPath", "Thumbnail of the event", JsonFieldType.STRING),
        DocumentedJSONField("registerUrl", "Link to the event registration", JsonFieldType.STRING, optional = true),
        DocumentedJSONField("dateInterval.startDate", "Event beginning date", JsonFieldType.STRING),
        DocumentedJSONField("dateInterval.endDate", "Event finishing date", JsonFieldType.STRING, optional = true),
        DocumentedJSONField("location", "Location for the event", JsonFieldType.STRING, optional = true),
        DocumentedJSONField("category", "Event category", JsonFieldType.STRING, optional = true),
        DocumentedJSONField(
            "slug",
            "Short and friendly textual event identifier",
            JsonFieldType.STRING,
            optional = true
        ),
        DocumentedJSONField("id", "Event ID", JsonFieldType.NUMBER, isInRequest = false),
        DocumentedJSONField(
            "teamMembers",
            "Array of members associated with the event",
            JsonFieldType.ARRAY,
            isInRequest = false
        ),
        DocumentedJSONField(
            "teamMembersIds[]",
            "Team member IDs",
            JsonFieldType.ARRAY,
            isInResponse = false
        )
    ).addFieldsBeneathPath(
        "teamMembers[]",
        PayloadAccount().payload.documentedJSONFields,
        optional = true,
        addResponse = true
    )
)
