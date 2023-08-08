package pt.up.fe.ni.website.backend.dto.entity

import pt.up.fe.ni.website.backend.model.CustomWebsite

class CustomWebsiteDto(
    val url: String,
    val iconPath: String?,
    val label: String?
) : EntityDto<CustomWebsite>()
