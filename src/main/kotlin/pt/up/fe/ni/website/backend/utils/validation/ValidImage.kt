package pt.up.fe.ni.website.backend.utils.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass
import org.springframework.web.multipart.MultipartFile
import pt.up.fe.ni.website.backend.model.constants.UploadConstants
import pt.up.fe.ni.website.backend.utils.extensions.filenameExtension

@MustBeDocumented
@Constraint(validatedBy = [ValidImageValidator::class])
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidImage(
    val message: String = "{files.invalid_image}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<Payload>> = []
)

class ValidImageValidator : ConstraintValidator<ValidImage, MultipartFile?> {

    override fun isValid(value: MultipartFile?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) {
            return true
        }

        if (!isSupportedContentType(value.contentType)) {
            return false
        }

        if (!isSupportedFileExtension(value.filenameExtension())) {
            return false
        }

        return true
    }

    private fun isSupportedContentType(contentType: String?): Boolean {
        return UploadConstants.SupportedTypes.contentTypes.contains(contentType)
    }

    private fun isSupportedFileExtension(extension: String?): Boolean {
        return UploadConstants.SupportedTypes.fileExtensions.contains(extension)
    }
}
