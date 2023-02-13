package pt.up.fe.ni.website.backend.dto.entity

import pt.up.fe.ni.website.backend.model.Post

class PostDto(
    val title: String,
    val body: String,
    val thumbnailPath: String,
    val slug: String?
) : EntityDto<Post>()
