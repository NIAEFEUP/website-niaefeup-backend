package pt.up.fe.ni.website.backend.documentation.payloadschemas.model

import org.springframework.restdocs.payload.JsonFieldType
import pt.up.fe.ni.website.backend.documentation.Tag
import pt.up.fe.ni.website.backend.utils.documentation.DocumentedJSONField
import pt.up.fe.ni.website.backend.utils.documentation.ModelDocumentation

class PayloadPost : ModelDocumentation(
    Tag.POST.name.lowercase(),
    Tag.POST,
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
            "Post's publish date",
            JsonFieldType.STRING,
            isInRequest = false
        ),
        DocumentedJSONField(
            "lastUpdatedAt",
            "Post's latest update",
            JsonFieldType.STRING,
            isInRequest = false
        )
    )
)
