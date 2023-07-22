package pt.up.fe.ni.website.backend.utils.documentation.payloadschemas.model

import org.springframework.restdocs.payload.JsonFieldType
import pt.up.fe.ni.website.backend.utils.documentation.Tag
import pt.up.fe.ni.website.backend.utils.documentation.utils.DocumentedJSONField
import pt.up.fe.ni.website.backend.utils.documentation.utils.DocumentedJSONField.Companion.addFieldsBeneathPath
import pt.up.fe.ni.website.backend.utils.documentation.utils.ModelDocumentation

class PayloadProject : ModelDocumentation(
    Tag.PROJECT.name.lowercase(),
    Tag.PROJECT,
    mutableListOf(
        DocumentedJSONField("title", "Project title", JsonFieldType.STRING),
        DocumentedJSONField("description", "Project description", JsonFieldType.STRING),
        DocumentedJSONField("isArchived", "If the project is no longer maintained", JsonFieldType.BOOLEAN),
        DocumentedJSONField(
            "technologies",
            "Array of technologies used in the project",
            JsonFieldType.ARRAY,
            optional = true
        ),
        DocumentedJSONField("technologies.*", "Technology", JsonFieldType.STRING, optional = true),
        DocumentedJSONField(
            "slug",
            "Short and friendly textual event identifier",
            JsonFieldType.STRING,
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
            "Slogan of the project",
            JsonFieldType.STRING
        ),
        DocumentedJSONField("id", "Project ID", JsonFieldType.NUMBER, isInRequest = false),
        DocumentedJSONField(
            "hallOfFame",
            "Array of members that were once associated with the project",
            JsonFieldType.ARRAY,
            optional = true
        ),
        DocumentedJSONField(
            "hallOfFameIds",
            "Array with IDs of members that were once associated with the project",
            JsonFieldType.ARRAY,
            optional = true,
            isInResponse = false
        ),
        DocumentedJSONField(
            "teamMembers",
            "Array of members associated with the project",
            JsonFieldType.ARRAY,
            isInRequest = false
        ),
        DocumentedJSONField(
            "teamMembersIds",
            "Array with IDs of members associated with the project",
            JsonFieldType.ARRAY,
            optional = true,
            isInResponse = false
        ),
        DocumentedJSONField(
            "teamMembersIds.*",
            "Account ID",
            JsonFieldType.NUMBER,
            optional = true,
            isInResponse = false
        ),
        DocumentedJSONField(
            "links",
            "Array of links associated with the project",
            JsonFieldType.ARRAY,
            optional = true
        ),
        DocumentedJSONField("links[].id", "ID of the link", JsonFieldType.NUMBER, optional = true, isInRequest = false),
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
            "Array of events defining the project's timeline",
            JsonFieldType.ARRAY,
            optional = true
        ),
        DocumentedJSONField(
            "timeline[].id",
            "ID of the event",
            JsonFieldType.NUMBER,
            optional = true,
            isInRequest = false
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
        "teamMembers[]",
        PayloadAccount().payload.documentedJSONFields,
        addResponse = true,
        optional = true
    ).addFieldsBeneathPath(
        "hallOfFame[]",
        PayloadAccount().payload.documentedJSONFields,
        addResponse = true,
        optional = true
    )
)
