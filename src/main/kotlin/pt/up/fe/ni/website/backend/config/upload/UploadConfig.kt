package pt.up.fe.ni.website.backend.config.upload

import com.cloudinary.Cloudinary
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.HandlerExceptionResolver
import pt.up.fe.ni.website.backend.util.CloudinaryFileUploader
import pt.up.fe.ni.website.backend.util.FileUploader
import pt.up.fe.ni.website.backend.util.StaticFileUploader

@Configuration
class UploadConfig(
    private val uploadConfigProperties: UploadConfigProperties,
    @Qualifier("handlerExceptionResolver") val exceptionResolver: HandlerExceptionResolver

) {
    @Bean
    fun fileUploader(): FileUploader {
        return when (uploadConfigProperties.uploadType) {
            "cloudinary" -> CloudinaryFileUploader(
                Cloudinary(
                    uploadConfigProperties.cloudinaryUrl
                        ?: kotlin.run { throw Error("Cloudinary URL not provided") }
                )
            )
            else -> StaticFileUploader(uploadConfigProperties.staticPath ?: "")
        }
    }
}
