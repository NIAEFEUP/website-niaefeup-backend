package pt.up.fe.ni.website.backend.dto.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.web.multipart.MultipartFile
import pt.up.fe.ni.website.backend.model.Activity

abstract class ActivityDto<T : Activity>(
    val title: String,
    val description: String,
    val teamMembersIds: List<Long>?,
    val slug: String?,
    var image: String?,
    @JsonIgnore
    var imageFile: MultipartFile? = null
) : EntityDto<T>()
