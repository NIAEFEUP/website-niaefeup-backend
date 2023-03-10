package pt.up.fe.ni.website.backend.documentation.payloadschemas.model

import org.springframework.restdocs.payload.JsonFieldType
import pt.up.fe.ni.website.backend.documentation.Tag
import pt.up.fe.ni.website.backend.utils.documentation.DocumentedJSONField
import pt.up.fe.ni.website.backend.utils.documentation.DocumentedJSONField.Companion.addFieldsBeneathPath
import pt.up.fe.ni.website.backend.utils.documentation.ModelDocumentation

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
        DocumentedJSONField(
            "associatedRoles[]",
            "An activity that aggregates members with different roles",
            JsonFieldType.ARRAY,
            optional = true
        ),
        DocumentedJSONField(
            "slug",
            "Short and friendly textual event identifier",
            JsonFieldType.STRING,
            optional = true
        ),
        DocumentedJSONField("id", "Project ID", JsonFieldType.NUMBER, isInRequest = false),
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
        "associatedRoles[]",
        PayloadAssociatedRoles.payload.documentedJSONFields,
        addResponse = true,
        optional = true
    ).addFieldsBeneathPath(
        "associatedRoles[]",
        PayloadAssociatedRoles.payload.documentedJSONFields,
        addResponse = true,
        addRequest = true,
        optional = true
    )

)
