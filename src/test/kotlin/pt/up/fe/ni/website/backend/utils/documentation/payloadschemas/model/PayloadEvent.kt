package pt.up.fe.ni.website.backend.utils.documentation.payloadschemas.model

import org.springframework.restdocs.payload.JsonFieldType
import pt.up.fe.ni.website.backend.utils.documentation.Tag
import pt.up.fe.ni.website.backend.utils.documentation.utils.DocumentedJSONField
import pt.up.fe.ni.website.backend.utils.documentation.utils.DocumentedJSONField.Companion.addFieldsBeneathPath
import pt.up.fe.ni.website.backend.utils.documentation.utils.ModelDocumentation

class PayloadEvent : ModelDocumentation(
    Tag.EVENT.name.lowercase(),
    Tag.EVENT,
    mutableListOf(
        DocumentedJSONField("title", "Event title", JsonFieldType.STRING),
        DocumentedJSONField("description", "Event description", JsonFieldType.STRING),
        DocumentedJSONField("thumbnailPath", "Thumbnail of the event", JsonFieldType.STRING),
        DocumentedJSONField("registerUrl", "Link to the event registration", JsonFieldType.STRING, optional = true),
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
    ).addFieldsBeneathPath(
        "",
        PayloadDateInterval.payload.documentedJSONFields,
        addRequest = true,
        addResponse = true
    )
)
