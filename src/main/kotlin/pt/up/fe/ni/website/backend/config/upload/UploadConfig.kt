package pt.up.fe.ni.website.backend.config.upload

import com.cloudinary.Cloudinary
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.ResourceUtils
import pt.up.fe.ni.website.backend.service.upload.CloudinaryFileUploader
import pt.up.fe.ni.website.backend.service.upload.FileUploader
import pt.up.fe.ni.website.backend.service.upload.StaticFileUploader

@Configuration
class UploadConfig(
    private val uploadConfigProperties: UploadConfigProperties
) {
    @Bean
    fun fileUploader(): FileUploader {
        return when (uploadConfigProperties.provider) {
            "cloudinary" -> CloudinaryFileUploader(
                uploadConfigProperties.cloudinaryBasePath ?: "/",
                Cloudinary(uploadConfigProperties.cloudinaryUrl ?: throw Error("Cloudinary URL not provided"))
            )
            else -> StaticFileUploader(
                uploadConfigProperties.staticPath?.let { ResourceUtils.getFile(it).absolutePath } ?: "",
                uploadConfigProperties.staticServe ?: "localhost:8080"
            )
        }
    }
}
