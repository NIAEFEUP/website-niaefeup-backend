package pt.up.fe.ni.website.backend.utils.extensions

import org.springframework.web.multipart.MultipartFile

fun MultipartFile.filenameExtension(): String? {
    return this.originalFilename?.substringAfterLast(".") ?: ""
}
