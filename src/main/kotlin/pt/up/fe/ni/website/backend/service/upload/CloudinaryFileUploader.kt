package pt.up.fe.ni.website.backend.service.upload

import com.cloudinary.Cloudinary
import com.cloudinary.Transformation

class CloudinaryFileUploader(private val basePath: String, private val cloudinary: Cloudinary) : FileUploader() {
    override fun uploadImage(folder: String, fileName: String, image: ByteArray): String {
        val path = "$basePath/$folder/$fileName"

        val imageTransformation = Transformation().width(250).height(250).crop("thumb").chain()

        val result = cloudinary.uploader().upload(
            image,
            mapOf(
                "public_id" to path,
                "overwrite" to true,
                "transformation" to imageTransformation
            )
        )

        return result["url"]?.toString() ?: ""
    }
}
