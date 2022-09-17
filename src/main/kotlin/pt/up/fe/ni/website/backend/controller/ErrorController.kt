package pt.up.fe.ni.website.backend.controller

import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.NoHandlerFoundException

data class SimpleError(
    val message: String,
    val param: String? = null,
    val value: Any? = null
)

data class CustomError(val errors: List<SimpleError>)

@RestControllerAdvice
class ErrorController : ErrorController {

    @ExceptionHandler(NoHandlerFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun endpointNotFound(): CustomError = wrapSimpleError("invalid endpoint")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun invalidArguments(e: MethodArgumentNotValidException): CustomError {
        val errors = mutableListOf<SimpleError>()
        e.bindingResult.allErrors.forEach { error: ObjectError ->
            val fieldError = (error as FieldError)
            errors.add(
                SimpleError(
                    error.defaultMessage ?: "invalid",
                    fieldError.field,
                    fieldError.rejectedValue
                )
            )
        }
        return CustomError(errors)
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun unexpectedError(e: Exception): CustomError = wrapSimpleError(e.message ?: "unexpected error")

    fun wrapSimpleError(msg: String, param: String? = null) = CustomError(
        mutableListOf(SimpleError(msg, param))
    )
}
