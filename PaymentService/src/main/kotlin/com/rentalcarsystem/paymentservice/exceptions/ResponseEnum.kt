package com.rentalcarsystem.paymentservice.exceptions

import org.springframework.http.HttpStatus
import java.util.*


enum class ResponseEnum(
    private val code: Int,
    private val description: String,
    private val httpStatus: HttpStatus
) {
    OK(200, "Success", HttpStatus.OK),
    FORBIDDEN(403, "Missing permissions", HttpStatus.FORBIDDEN),
    NOT_FOUND(404, "Resource not found", HttpStatus.NOT_FOUND),
    UNPROCESSABLE_ENTITY(422, "Input validation failed", HttpStatus.UNPROCESSABLE_ENTITY),
    UNEXPECTED_ERROR(500, "Unknown error", HttpStatus.INTERNAL_SERVER_ERROR),

    // PaymentRecord errors
    PAYMENT_NOT_FOUND(4100, "Payment not found", HttpStatus.NOT_FOUND),
    PAYMENT_RECORD_EXISTS(4101, "Active payment record already exists", HttpStatus.CONFLICT);

    // Getters
    fun getCode() = code
    fun getDescription() = description
    fun getHttpStatus() = httpStatus

    override fun toString(): String {
        // Replace underscores with spaces
        val result = name.replace('_', ' ').lowercase(Locale.getDefault())
        // Capitalize the first character and leave the rest lowercase
        return result.substring(0, 1).uppercase(Locale.getDefault()) + result.substring(1)
    }
}