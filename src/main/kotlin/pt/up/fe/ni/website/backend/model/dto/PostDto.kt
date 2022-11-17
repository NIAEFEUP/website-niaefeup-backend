package pt.up.fe.ni.website.backend.model.dto

import pt.up.fe.ni.website.backend.model.Post

class PostDto(
    val title: String,
    val body: String,
    val thumbnailPath: String
) : Dto<Post>()
