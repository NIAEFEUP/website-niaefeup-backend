package pt.up.fe.ni.website.backend.model.dto

import pt.up.fe.ni.website.backend.model.CustomWebsite

class CustomWebsiteDto(
    val url: String,
    val iconPath: String?
) : Dto<CustomWebsite>()
