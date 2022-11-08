package pt.up.fe.ni.website.backend.model.dto

import io.swagger.v3.oas.annotations.media.Schema
import pt.up.fe.ni.website.backend.model.Post
import pt.up.fe.ni.website.backend.model.constants.PostConstants as Constants

class PostDto(
    @field:Schema(
        description = "Title of the post",
        example = "Welcome new recruits",
        type = "string",
        minLength = Constants.Title.minSize,
        maxLength = Constants.Title.maxSize
    )
    val title: String,

    @field:Schema(
        description = "Body of the post",
        example = "We want to congratulate you on joining our nucleus, but there's a lot of work ahead still. ...",
        type = "string",
        minLength = Constants.Body.minSize,
    )
    val body: String,
    @field:Schema(
        description = "Path of the post image",
        type = "string",
    )
    val thumbnailPath: String
) : Dto<Post>()
