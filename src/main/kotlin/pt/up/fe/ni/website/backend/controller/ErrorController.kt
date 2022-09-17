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

typealias MultipleErrors = MutableList<Map<String, Any?>>
typealias CustomError = Map<String, MultipleErrors>

@RestControllerAdvice
class ErrorController : ErrorController {
    val errorKey: String = "errors"

    @ExceptionHandler(NoHandlerFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun endpointNotFound(): CustomError = wrapSimpleError("invalid endpoint")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun invalidArguments(e: MethodArgumentNotValidException): CustomError {
        val errors: MultipleErrors = mutableListOf()
        e.bindingResult.allErrors.forEach {error: ObjectError ->
            val fieldError = (error as FieldError)
            errors.add(mapOf(
                "param" to fieldError.field,
                "message" to error.defaultMessage,
                "value" to fieldError.rejectedValue
            ))
        }
        return mapOf(errorKey to errors)
    }

    fun wrapSimpleError(msg: String): CustomError = mapOf(
        errorKey to mutableListOf(mapOf("message" to msg))
    )
}
