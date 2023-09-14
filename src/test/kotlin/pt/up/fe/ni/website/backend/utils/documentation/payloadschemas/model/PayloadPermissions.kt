package pt.up.fe.ni.website.backend.utils.documentation.payloadschemas.model

import org.springframework.restdocs.payload.JsonFieldType
import pt.up.fe.ni.website.backend.utils.documentation.Tag
import pt.up.fe.ni.website.backend.utils.documentation.utils.DocumentedJSONField
import pt.up.fe.ni.website.backend.utils.documentation.utils.ModelDocumentation

class PayloadPermissions : ModelDocumentation(
    Tag.ROLES.name.lowercase(),
    Tag.ROLES,
    mutableListOf(
        DocumentedJSONField("permissions", "Permissions array", JsonFieldType.ARRAY)
    )
)
