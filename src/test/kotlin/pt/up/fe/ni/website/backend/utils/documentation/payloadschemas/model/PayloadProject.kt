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
        DocumentedJSONField("id", "Project ID", JsonFieldType.NUMBER, isInRequest = false),
        DocumentedJSONField(
            "hallOfFame",
            "Array of members that were once associated with the project",
            JsonFieldType.ARRAY,
            optional = true
        ),
        DocumentedJSONField(
            "hallOfFame",
            "Array of members that were once associated with the project",
            isInRequest = true
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
