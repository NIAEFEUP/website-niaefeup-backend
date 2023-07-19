package pt.up.fe.ni.website.backend.utils.documentation.payloadschemas.model

import org.springframework.restdocs.payload.JsonFieldType
import pt.up.fe.ni.website.backend.utils.documentation.Tag
import pt.up.fe.ni.website.backend.utils.documentation.utils.DocumentedJSONField
import pt.up.fe.ni.website.backend.utils.documentation.utils.ModelDocumentation

class PayloadRoles : ModelDocumentation(
    Tag.ROLES.name.lowercase(),
    Tag.ROLES,
    mutableListOf(
        DocumentedJSONField("name", "Name of the role", JsonFieldType.STRING),
        DocumentedJSONField("permissions", "Permissions in the role, as an integer", JsonFieldType.ARRAY),
        DocumentedJSONField("isSection", "Whether the role should be displayed as a section", JsonFieldType.BOOLEAN),
        DocumentedJSONField("id", "Internal ID of role", JsonFieldType.NUMBER),
        DocumentedJSONField(
            "associatedActivities",
            "List of activities that are associated with this role",
            JsonFieldType.ARRAY
        ),
        DocumentedJSONField(
            "generation",
            "ID of generation that this role should be added to, this is optional and if not specified it " +
                "defaults to the latest generation",
            JsonFieldType.NUMBER,
            optional = true
        )
    )
)
