package com.rentalcarsystem.paymentservice.controllers

import com.rentalcarsystem.paymentservice.dtos.request.PaymentReqDTO
import com.rentalcarsystem.paymentservice.dtos.response.PagedResDTO
import com.rentalcarsystem.paymentservice.dtos.response.PaymentRecordResDTO
import com.rentalcarsystem.paymentservice.dtos.response.PaymentResDTO
import com.rentalcarsystem.paymentservice.dtos.response.toResDTO
import com.rentalcarsystem.paymentservice.filters.PaymentRecordFilter
import com.rentalcarsystem.paymentservice.services.PaymentRecordService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize

@RestController
@RequestMapping("/api/v1/payments/paypal")
@CrossOrigin(origins = ["*"])
class PaymentRecordController(private val paymentRecordService: PaymentRecordService) {

    @PostMapping("/create")
    fun createPayPalOrder(
        @RequestBody paymentReqDTO: PaymentReqDTO
    ): ResponseEntity<PaymentResDTO> {
        val createdPayment = paymentRecordService.createPayPalOrder(paymentReqDTO)
        return ResponseEntity.ok(createdPayment)
    }

    @GetMapping("/capture")
    fun capturePayPalOrder(
        @RequestParam("token") token: String,
        @RequestParam("PayerID") payerID: String,
        response: HttpServletResponse
    ): String {
        paymentRecordService.capturePayPalOrder(token, payerID)
        val record = paymentRecordService.getPaymentRecordByToken(token)
        response.sendRedirect("/orders/$token?reservationId=${record.reservationId}&completed=true")
        return "redirect:/orders/$token?reservationId=${record.reservationId}&completed=true"
    }

    @GetMapping("/cancel")
    fun cancelPayPalOrder(
        @RequestParam("token") token: String,
        response: HttpServletResponse
    ): String {
        paymentRecordService.cancelPayPalOrder(token)
        val record = paymentRecordService.getPaymentRecordByToken(token)
        response.sendRedirect("/orders/$token?reservationId=${record.reservationId}&completed=false")
        return "redirect:/orders/$token?reservationId=${record.reservationId}&completed=false"
    }

    @GetMapping("/order/reservation/{reservationId}")
    fun getPaymentRecordByReservationId(@PathVariable reservationId: Long): ResponseEntity<PaymentRecordResDTO> {
        require(reservationId > 0) { "Invalid reservation id $reservationId: it must be a positive number" }
        return ResponseEntity.ok(paymentRecordService.getPaymentRecordByReservationId(reservationId).toResDTO())
    }

    @GetMapping("/order/token/{token}")
    fun getPaymentRecordByToken(@PathVariable token: String): ResponseEntity<PaymentRecordResDTO> {
        require(token.isNotBlank()) { "Invalid token $token: it must not be blank" }
        return ResponseEntity.ok(paymentRecordService.getPaymentRecordByToken(token).toResDTO())
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/records")
    fun getPaymentRecords(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int,
        @RequestParam("sort", defaultValue = "token") sortBy: String,
        @RequestParam("order", defaultValue = "asc") sortOrder: String,
        @ModelAttribute filters: PaymentRecordFilter
    ): ResponseEntity<PagedResDTO<PaymentRecordResDTO>> {
        require(page >= 0) { "Parameter 'page' must be greater than or equal to zero" }
        require(size > 0) { "Parameter 'size' must be greater than zero" }
        val allowedSortFields = listOf(
            "reservationId",
            "customerId",
            "amount",
            "token",
            "status"
        )
        require(sortBy in allowedSortFields) { "Parameter 'sort' invalid. Allowed values: $allowedSortFields" }
        require(sortOrder in listOf("asc", "desc")) { "Parameter 'sortOrder' invalid. Allowed values: ['asc', 'desc']" }
        return ResponseEntity.ok(
            paymentRecordService.getPaymentRecords(
                page, size, sortBy, sortOrder, filters
            )
        )
    }
}
