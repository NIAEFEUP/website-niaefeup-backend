package pt.up.fe.ni.website.backend.service.upload

import java.io.File

class StaticFileUploader(private val storePath: String, private val servePath: String) : FileUploader() {
    override fun uploadImage(folder: String, fileName: String, image: ByteArray): String {
        val file = File("$storePath/$folder/$fileName")
        file.createNewFile()
        file.writeBytes(image)

        return "$servePath/$folder/$fileName"
    }
}
