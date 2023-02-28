package pt.up.fe.ni.website.backend.documentation.payloadschemas.model

import org.springframework.restdocs.payload.JsonFieldType
import pt.up.fe.ni.website.backend.utils.documentation.DocumentedJSONField
import pt.up.fe.ni.website.backend.utils.documentation.ModelDocumentation

class Post : ModelDocumentation(
    Companion.Tag.POST.name.lowercase(),
    Companion.Tag.POST,
    mutableListOf(
        DocumentedJSONField("title", "Post title", JsonFieldType.STRING),
        DocumentedJSONField("body", "Post body", JsonFieldType.STRING),
        DocumentedJSONField("thumbnailPath", "Path for the post thumbnail image", JsonFieldType.STRING),
        DocumentedJSONField(
            "slug",
            "Short and friendly textual post identifier",
            JsonFieldType.STRING,
            optional = true
        ),
        DocumentedJSONField("id", "Post ID", JsonFieldType.NUMBER, isInRequest = false),
        DocumentedJSONField(
            "publishDate",
            "Date of publication of the post",
            JsonFieldType.STRING,
            isInRequest = false
        ),
        DocumentedJSONField(
            "lastUpdatedAt",
            "Date of the last update of the post",
            JsonFieldType.STRING,
            isInRequest = false
        )
    )
)
