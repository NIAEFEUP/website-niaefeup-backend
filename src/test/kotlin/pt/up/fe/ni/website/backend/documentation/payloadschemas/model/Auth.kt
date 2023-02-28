package pt.up.fe.ni.website.backend.documentation.payloadschemas.model

import org.springframework.restdocs.payload.JsonFieldType
import pt.up.fe.ni.website.backend.utils.documentation.DocumentedJSONField
import pt.up.fe.ni.website.backend.utils.documentation.DocumentedJSONField.Companion.addFieldsBeneathPath
import pt.up.fe.ni.website.backend.utils.documentation.ModelDocumentation

class AuthNew : ModelDocumentation(
    Companion.Tag.AUTH.name.lowercase() + "-new",
    Companion.Tag.AUTH,
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

class AuthRefresh : ModelDocumentation(
    Companion.Tag.AUTH.name.lowercase() + "-refresh",
    Companion.Tag.AUTH,
    mutableListOf(
        DocumentedJSONField(
            "token",
            "Access token, used to identify the user",
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

class AuthCheck : ModelDocumentation(
    Companion.Tag.AUTH.name.lowercase() + "-check",
    Companion.Tag.AUTH,
    mutableListOf(
        DocumentedJSONField(
            "authenticated_user",
            "Authenticated account's information.",
            JsonFieldType.OBJECT,
            isInRequest = false
        )
    ).addFieldsBeneathPath("authenticated_user", Account().payload.documentedJSONFields, addResponse = true)
)
