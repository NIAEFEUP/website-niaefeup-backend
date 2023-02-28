package pt.up.fe.ni.website.backend.documentation.payloadschemas.model

import org.springframework.restdocs.payload.JsonFieldType
import pt.up.fe.ni.website.backend.utils.documentation.DocumentedJSONField
import pt.up.fe.ni.website.backend.utils.documentation.DocumentedJSONField.Companion.addFieldsBeneathPath
import pt.up.fe.ni.website.backend.utils.documentation.ModelDocumentation

class Event : ModelDocumentation(
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
        DocumentedJSONField("associatedRoles[]", "Array of Roles/Activity associations", isInRequest = false),
        DocumentedJSONField(
            "associatedRoles[].*.role",
            "Roles associated with the activity",
            JsonFieldType.OBJECT,
            optional = true,
            isInRequest = false
        ),
        DocumentedJSONField(
            "associatedRoles[].*.activity",
            "An activity that aggregates members with different roles",
            JsonFieldType.OBJECT,
            optional = true,
            isInRequest = false
        ),
        DocumentedJSONField(
            "associatedRoles[].*.permissions",
            "Permissions of someone with a given role for this activity",
            JsonFieldType.OBJECT,
            optional = true,
            isInRequest = false
        ),
        DocumentedJSONField(
            "associatedRoles[].*.id",
            "Id of the role/activity association",
            JsonFieldType.NUMBER,
            optional = true,
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
        Account().payload.documentedJSONFields,
        optional = true,
        addResponse = true
    )
)
