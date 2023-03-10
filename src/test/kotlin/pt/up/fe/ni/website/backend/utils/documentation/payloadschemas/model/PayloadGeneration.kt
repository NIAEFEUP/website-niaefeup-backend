package pt.up.fe.ni.website.backend.utils.documentation.payloadschemas.model

import org.springframework.restdocs.payload.JsonFieldType
import pt.up.fe.ni.website.backend.utils.documentation.Tag
import pt.up.fe.ni.website.backend.utils.documentation.utils.DocumentedJSONField
import pt.up.fe.ni.website.backend.utils.documentation.utils.DocumentedJSONField.Companion.addFieldsBeneathPath
import pt.up.fe.ni.website.backend.utils.documentation.utils.ModelDocumentation

class PayloadGeneration : ModelDocumentation(
    Tag.GENERATION.name.lowercase(),
    Tag.GENERATION,
    mutableListOf(
        DocumentedJSONField("schoolYear", "School year of the generation", JsonFieldType.STRING, optional = true),
        DocumentedJSONField("id", "Id of the generation", JsonFieldType.NUMBER, isInRequest = false),
        DocumentedJSONField("roles", "Roles associated with the generation", JsonFieldType.ARRAY)
    ).addFieldsBeneathPath(
        "roles[]",
        PayloadAssociatedRoles.payload.documentedJSONFields,
        optional = true,
        addResponse = true,
        addRequest = true
    )
)

class PayloadGenerationYears : ModelDocumentation(
    "arrayOf-${Tag.GENERATION.name.lowercase()}-years",
    Tag.GENERATION,
    mutableListOf(
        DocumentedJSONField("[]", "List of all generation years", JsonFieldType.ARRAY),
        DocumentedJSONField("[].*", "School year", JsonFieldType.STRING, optional = true)
    )
)

class PayloadGenerationGenerationSections : ModelDocumentation(
    Tag.GENERATION.name.lowercase(),
    Tag.GENERATION,
    mutableListOf(
        DocumentedJSONField("[]", "Generation sections", JsonFieldType.ARRAY, isInRequest = false),
        DocumentedJSONField("[].section", "Section role name", JsonFieldType.STRING, isInRequest = false),
        DocumentedJSONField(
            "[].accounts[]",
            "Array of accounts of with the section role",
            JsonFieldType.ARRAY,
            isInRequest = false
        )
    ).addFieldsBeneathPath(
        "[].accounts[]",
        PayloadAccount().payload.documentedJSONFields,
        optional = true,
        addResponse = true
    )
)
