package com.rentalcarsystem.analyticsservice.exceptions

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.*
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.net.URI
import java.net.URISyntaxException


@RestControllerAdvice
class ProblemDetailHandler : ResponseEntityExceptionHandler() {
    // Triggered when the requested element is not found in the database
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(e: NoSuchElementException): ProblemDetail {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND)
        problemDetail.title = "Not found"
        problemDetail.detail = e.message
        return problemDetail
    }

    // Triggered when the provided Path Variable or Request Parameter is not convertible to the expected type
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(e: MethodArgumentTypeMismatchException): ProblemDetail {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        problemDetail.title = "Bad request"
        problemDetail.detail = e.message
        return problemDetail
    }

    // Triggered when the provided Request Parameter in @ModelAttribute has a field that is not convertible to the expected type
    // Overrides Spring's default error handler for MethodArgumentNotValidException
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        problemDetail.title = "Bad request"
        // Unlike in Spring's default handler, problemDetail.detail now contains the error message that identifies the field that caused the error
        // Extract only the default message(s) from field errors
        val fieldErrors = ex.bindingResult.fieldErrors
        val errorMessages = fieldErrors.map { it.defaultMessage ?: "Invalid value for field '${it.field}'" }
        // Combine into a single message if multiple
        problemDetail.detail = errorMessages.joinToString("; ")
        return ResponseEntity(problemDetail, HttpStatus.BAD_REQUEST)
    }

    // Triggered when the provided request body has at least one field that is not convertible to the expected type
    // Overrides Spring's default error handler for HttpMessageNotReadableException
    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        problemDetail.title = "Bad request"
        // Unlike in Spring's default handler, problemDetail.detail now contains the error message that identifies the field that caused the error
        problemDetail.detail = ex.message
        return ResponseEntity(problemDetail, HttpStatus.BAD_REQUEST)
    }

    // Triggered when the provided Path Variable or Request Parameter is convertible to the expected type but does not satisfy the constraints
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ProblemDetail {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY)
        problemDetail.title = "Unprocessable entity"
        problemDetail.detail = e.message
        return problemDetail
    }

    // Triggered when the provided request body has at least one field that is convertible to the expected type but does not satisfy the constraints
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(e: ConstraintViolationException): ProblemDetail {
        val errors: MutableMap<String, Any> = HashMap()
        e.constraintViolations.forEach { errors[it.propertyPath.toString()] = it.message }
        val problemDetail = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY)
        problemDetail.title = "Unprocessable entity"
        problemDetail.properties = errors
        return problemDetail
    }

    // Triggered when the element is already present in the database and cannot allow a duplicate to be added
    @ExceptionHandler(DuplicateKeyException::class)
    fun handleDuplicateKeyException(e: DuplicateKeyException): ProblemDetail {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT)
        problemDetail.title = "Conflict"
        problemDetail.detail = e.message
        return problemDetail
    }

    @ExceptionHandler(FailureException::class)
    @Throws(URISyntaxException::class)
    fun handleFailureException(ex: FailureException, request: HttpServletRequest): ProblemDetail {
        val problemDetail = ProblemDetail.forStatusAndDetail(
            ex.getHttpStatus(),
            ex.message ?: ex.getResponseEnum().getDescription()
        )
        problemDetail.type = URI("https://smart-travel.com/errors/${ex.getResponseEnum().name.lowercase()}")
        problemDetail.title =
            if (!ex.message.isNullOrBlank()) ex.getResponseEnum().getDescription() else ex.getResponseEnum().toString()
        problemDetail.instance = URI.create(request.requestURI)
        return problemDetail
    }
}