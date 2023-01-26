package pt.up.fe.ni.website.backend.util

import org.springframework.web.multipart.MultipartFile

fun MultipartFile.filenameExtension(): String? {
    return this.originalFilename?.substringAfterLast(".") ?: ""
}
