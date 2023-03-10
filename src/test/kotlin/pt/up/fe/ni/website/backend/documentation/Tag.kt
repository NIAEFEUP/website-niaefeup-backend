package pt.up.fe.ni.website.backend.documentation

import pt.up.fe.ni.website.backend.utils.documentation.ITag

enum class Tag(override val fullName: String) : ITag {
    AUTH("Authentication"),
    ACCOUNT("Accounts"),
    EVENT("Events"),
    GENERATION("Generations"),
    POST("Posts"),
    PROJECT("Projects")
}
