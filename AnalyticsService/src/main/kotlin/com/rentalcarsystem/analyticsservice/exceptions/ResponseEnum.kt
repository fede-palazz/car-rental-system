package com.rentalcarsystem.analyticsservice.exceptions

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
    INVALID_EVENT_TYPE(501, "Invalid event type", HttpStatus.BAD_REQUEST),

    // Car Model errors
    CAR_MODEL_NOT_FOUND(4100, "Car model not found", HttpStatus.NOT_FOUND),
    WRONG_CAR_MODEL_COMPOSITE_ID(4101, "Wrong car model composite id", HttpStatus.BAD_REQUEST),
    CAR_MODEL_NOT_PROVIDED(4102, "Car model not provided", HttpStatus.BAD_REQUEST),

    // Maintenance errors
    MAINTENANCE_NOT_PROVIDED(4200, "Maintenance not provided", HttpStatus.BAD_REQUEST),
    MAINTENANCE_NOT_FOUND(4201, "Maintenance not found", HttpStatus.NOT_FOUND),

    // Reservation errors
    RESERVATION_NOT_FOUND(4300, "Reservation not found", HttpStatus.NOT_FOUND),

    // Vehicle errors
    VEHICLE_NOT_FOUND(4400, "Vehicle not found", HttpStatus.NOT_FOUND);

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