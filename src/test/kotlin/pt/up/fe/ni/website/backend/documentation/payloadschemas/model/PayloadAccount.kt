package pt.up.fe.ni.website.backend.documentation.payloadschemas.model

import org.springframework.restdocs.payload.JsonFieldType
import pt.up.fe.ni.website.backend.documentation.Tag
import pt.up.fe.ni.website.backend.utils.documentation.DocumentedJSONField
import pt.up.fe.ni.website.backend.utils.documentation.ModelDocumentation

class PayloadAccount : ModelDocumentation(
    Tag.ACCOUNT.name.lowercase(),
    Tag.ACCOUNT,
    mutableListOf(
        DocumentedJSONField("name", "Name of the account owner", JsonFieldType.STRING),
        DocumentedJSONField("email", "Email associated to the account", JsonFieldType.STRING),
        DocumentedJSONField("bio", "Short profile description", JsonFieldType.STRING, optional = true),
        DocumentedJSONField("birthDate", "Birth date of the owner", JsonFieldType.STRING, optional = true),
        DocumentedJSONField(
            "photoPath",
            "Path to the photo resource",
            JsonFieldType.STRING,
            optional = true
        ),
        DocumentedJSONField(
            "linkedin",
            "Handle/link to the owner's LinkedIn profile",
            JsonFieldType.STRING,
            optional = true
        ),
        DocumentedJSONField(
            "github",
            "Handle/link to the owner's GitHub profile",
            JsonFieldType.STRING,
            optional = true
        ),
        DocumentedJSONField(
            "websites[]",
            "Array with relevant websites about the owner",
            JsonFieldType.ARRAY,
            optional = true
        ),
        DocumentedJSONField("websites[].url", "URL to the website", JsonFieldType.STRING, optional = true),
        DocumentedJSONField("websites[].iconPath", "URL to the website's icon", JsonFieldType.STRING, optional = true),
        DocumentedJSONField(
            "roles[]",
            "Array with the roles of the account",
            JsonFieldType.ARRAY,
            optional = true,
            isInRequest = false
        ),
        DocumentedJSONField("password", "Account password", JsonFieldType.STRING, isInResponse = false),
        DocumentedJSONField("id", "Account ID", JsonFieldType.NUMBER, isInRequest = false),
        DocumentedJSONField(
            "websites[].id",
            "Related website ID",
            JsonFieldType.NUMBER,
            optional = true,
            isInRequest = false
        )
    )
)
