package pt.up.fe.ni.website.backend.config.upload

import com.cloudinary.Cloudinary
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.ResourceUtils
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import pt.up.fe.ni.website.backend.util.CloudinaryFileUploader
import pt.up.fe.ni.website.backend.util.FileUploader
import pt.up.fe.ni.website.backend.util.StaticFileUploader

@Configuration
class UploadConfig(
    private val uploadConfigProperties: UploadConfigProperties,

) : WebMvcConfigurer {
    @Bean
    fun fileUploader(): FileUploader {
        return when (uploadConfigProperties.uploadType) {
            "cloudinary" -> CloudinaryFileUploader(
                uploadConfigProperties.cloudinaryBasePath ?: "/",
                Cloudinary(
                    uploadConfigProperties.cloudinaryUrl
                        ?: kotlin.run { throw Error("Cloudinary URL not provided") }
                )
            )
            else -> StaticFileUploader(
                uploadConfigProperties.staticPath?.let { ResourceUtils.getFile(it).absolutePath } ?: "",
                uploadConfigProperties.staticServe ?: "localhost:8080"
            )
        }
    }
}
