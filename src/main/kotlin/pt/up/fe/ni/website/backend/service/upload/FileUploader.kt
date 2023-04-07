package pt.up.fe.ni.website.backend.service.upload

interface FileUploader {
    fun uploadImage(folder: String, fileName: String, image: ByteArray): String
}

