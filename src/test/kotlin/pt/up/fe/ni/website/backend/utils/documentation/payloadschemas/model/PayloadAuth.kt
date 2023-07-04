package pt.up.fe.ni.website.backend.utils.documentation.payloadschemas.model

import org.springframework.restdocs.payload.JsonFieldType
import pt.up.fe.ni.website.backend.utils.documentation.Tag
import pt.up.fe.ni.website.backend.utils.documentation.utils.DocumentedJSONField
import pt.up.fe.ni.website.backend.utils.documentation.utils.DocumentedJSONField.Companion.addFieldsBeneathPath
import pt.up.fe.ni.website.backend.utils.documentation.utils.ModelDocumentation

class PayloadAuthNew : ModelDocumentation(
    Tag.AUTH.name.lowercase() + "-new",
    Tag.AUTH,
    mutableListOf(
        DocumentedJSONField(
            "email",
            "Email of the account",
            JsonFieldType.STRING,
            isInResponse = false
        ),
        DocumentedJSONField(
            "password",
            "Password of the account",
            JsonFieldType.STRING,
            isInResponse = false
        ),
        DocumentedJSONField(
            "access_token",
            "Access token, used to identify the user",
            JsonFieldType.STRING,
            isInRequest = false
        ),
        DocumentedJSONField(
            "refresh_token",
            "Refresh token, used to refresh the access token",
            JsonFieldType.STRING,
            isInRequest = false
        )
    )
)

class PayloadAuthRefresh : ModelDocumentation(
    Tag.AUTH.name.lowercase() + "-refresh",
    Tag.AUTH,
    mutableListOf(
        DocumentedJSONField(
            "token",
            "Refresh token, used to refresh the access token",
            JsonFieldType.STRING,
            isInResponse = false
        ),
        DocumentedJSONField(
            "access_token",
            "Access token, used to identify the user",
            JsonFieldType.STRING,
            isInRequest = false
        )
    )
)

class PayloadRecoverPassword : ModelDocumentation(
    Tag.AUTH.name.lowercase() + "-recover",
    Tag.AUTH,
    mutableListOf(
        DocumentedJSONField(
            "email",
            "Email of the account",
            JsonFieldType.STRING,
            isInResponse = false
        ),
        DocumentedJSONField(
            "recovery_url",
            "URL to recover the password",
            JsonFieldType.STRING,
            isInRequest = false,
            optional = true // change this when email service is implemented
        )
    )
)

class PayloadAuthCheck : ModelDocumentation(
    Tag.AUTH.name.lowercase() + "-check",
    Tag.AUTH,
    mutableListOf(
        DocumentedJSONField(
            "authenticated_user",
            "Authenticated account's information.",
            JsonFieldType.OBJECT,
            isInRequest = false
        )
    ).addFieldsBeneathPath("authenticated_user", PayloadAccount().payload.documentedJSONFields, addResponse = true)
)
