package com.rentalcarsystem.reservationservice.exceptions

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

    // Car model errors
    CAR_MODEL_NOT_FOUND(4100, "Car model not found", HttpStatus.NOT_FOUND),
    CAR_MODEL_DUPLICATED(4101, "Duplicated car model", HttpStatus.CONFLICT),
    CAR_MODEL_NOT_AVAILABLE(4102, "Car model not available", HttpStatus.CONFLICT),

    // Vehicle errors
    VEHICLE_NOT_FOUND(4200, "Vehicle not found", HttpStatus.NOT_FOUND),
    VEHICLE_DUPLICATED(4201, "Duplicated vehicle", HttpStatus.CONFLICT),
    VEHICLE_NOT_AVAILABLE(4202, "Vehicle not available", HttpStatus.CONFLICT),

    // Maintenance errors
    MAINTENANCE_NOT_FOUND(4300, "Maintenance record not found", HttpStatus.NOT_FOUND),

    // Note errors
    NOTE_NOT_FOUND(4400, "Note not found", HttpStatus.NOT_FOUND),

    // Reservation errors
    RESERVATION_NOT_FOUND(4400, "Requested reservation not found", HttpStatus.NOT_FOUND),
    RESERVATION_INSUFFICIENT_SCORE(4401, "Insufficient score", HttpStatus.CONFLICT),
    RESERVATION_FORBIDDEN(4402, "Forbidden reservation", HttpStatus.FORBIDDEN),
    RESERVATION_PENDING(4403, "Pending reservation", HttpStatus.CONFLICT),

    // Payment error
    PAYMENT_ERROR(4500, "Payment error", HttpStatus.INTERNAL_SERVER_ERROR);

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