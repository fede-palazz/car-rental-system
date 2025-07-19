package com.rentalcarsystem.paymentservice.exceptions

import org.springframework.http.HttpStatus
import java.io.Serial


class FailureException : RuntimeException {
    private val httpStatus: HttpStatus
    private val responseEnum: ResponseEnum

    constructor(responseEnum: ResponseEnum, message: String? = "", exception: Throwable?) : super(
        message,
        exception
    ) {
        this.httpStatus = responseEnum.getHttpStatus()
        this.responseEnum = responseEnum
    }

    constructor(responseEnum: ResponseEnum, message: String? = "") : super(message) {
        this.httpStatus = responseEnum.getHttpStatus()
        this.responseEnum = responseEnum
    }

    fun getResponseEnum(): ResponseEnum = responseEnum

    fun getHttpStatus(): HttpStatus = httpStatus

    override fun toString(): String {
        return "FailureException {httpStatus=$httpStatus, responseErrorEnum=$responseEnum}"
    }

    companion object {
        @Serial
        private const val serialVersionUID = 1L
    }
}
