package pt.up.fe.ni.website.backend.util

import com.cloudinary.Cloudinary

interface FileUploader {
    fun upload(folder: String, fileName: String, image: ByteArray): String
    fun delete(filePath: String)
}

class CloudinaryFileUploader(val cloudinary: Cloudinary) : FileUploader {
    override fun upload(folder: String, fileName: String, image: ByteArray): String {
        println("upload cloud")

        return "$folder/$fileName"
    }

    override fun delete(filePath: String) {
        println("upload cloud")
    }
}

class StaticFileUploader(private val path: String) : FileUploader {
    override fun upload(folder: String, fileName: String, image: ByteArray): String {
        println("upload static")

        return "$path/$folder/$fileName"
    }

    override fun delete(filePath: String) {
        println("delete static")
    }
}
