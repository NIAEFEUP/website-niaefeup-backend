package pt.up.fe.ni.website.backend.service.upload

import java.util.UUID
import org.springframework.web.multipart.MultipartFile
import pt.up.fe.ni.website.backend.utils.extensions.filenameExtension

abstract class FileUploader {
    abstract fun uploadImage(folder: String, fileName: String, image: ByteArray): String

    fun buildFileName(photoFile: MultipartFile, prefix: String = ""): String =
        "$prefix-${UUID.randomUUID()}.${photoFile.filenameExtension()}"
}
