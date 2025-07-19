package com.rentalcarsystem.paymentservice.services

import com.rentalcarsystem.paymentservice.dtos.request.PaymentReqDTO
import com.rentalcarsystem.paymentservice.dtos.response.PagedResDTO
import com.rentalcarsystem.paymentservice.dtos.response.PaymentRecordResDTO
import com.rentalcarsystem.paymentservice.dtos.response.PaymentResDTO
import com.rentalcarsystem.paymentservice.dtos.response.toResDTO
import com.rentalcarsystem.paymentservice.exceptions.FailureException
import com.rentalcarsystem.paymentservice.exceptions.ResponseEnum
import com.rentalcarsystem.paymentservice.filters.PaymentRecordFilter
import com.rentalcarsystem.paymentservice.models.PaymentRecord
import com.rentalcarsystem.paymentservice.models.PaymentRecordStatus
import com.rentalcarsystem.paymentservice.repositories.PaymentRecordRepository
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated

@Service
@Validated
class PaymentRecordServiceImpl(
    private val payPalService: PayPalService,
    private val paymentRecordRepository: PaymentRecordRepository,
    private val updatePaymentService: UpdatePaymentService
) : PaymentRecordService {

    @Transactional
    override fun createPayPalOrder(@Valid paymentReqDTO: PaymentReqDTO): PaymentResDTO {
        if (paymentRecordRepository.existsByReservationIdAndStatusNot(paymentReqDTO.reservationId, PaymentRecordStatus.CANCELLED)) {
            throw FailureException(
                ResponseEnum.PAYMENT_RECORD_EXISTS,
                "Active payment record for reservation ${paymentReqDTO.reservationId} already exists"
            )
        }
        // Contact the PayPal API to create an order request
        val order = payPalService.createOrder(paymentReqDTO)!!
        val token = order.id
        // Create a payment record to be finalized afterward
        val paymentRecord = PaymentRecord(
            paymentReqDTO.reservationId,
            paymentReqDTO.customerUsername,
            paymentReqDTO.amount,
            token,
        )
        paymentRecordRepository.save(paymentRecord)
        // Return PayPal redirect URL
        return PaymentResDTO(order.links!![1].href)
    }

    override fun capturePayPalOrder(token: String, payerID: String) {
        // Get the payment record by token
        val paymentRecord = getPaymentRecordByToken(token)
        // Set the payment record status to PAID
        updatePaymentService.setPaid(token)
        // Create an outbox event for the PayPal order capture
        payPalService.createPayPalCaptureEvent(
            token, payerID, paymentRecord.getId()!!, paymentRecord.reservationId
        )
    }

    override fun cancelPayPalOrder(token: String) {
        updatePaymentService.setCancelled(token)
    }


    override fun getStatus(token: String): PaymentRecordStatus {
        return getPaymentRecordByToken(token).status
    }

    override fun getPaymentRecordByReservationId(reservationId: Long): PaymentRecord {
        return paymentRecordRepository.findByReservationId(reservationId).orElseThrow {
            FailureException(ResponseEnum.PAYMENT_NOT_FOUND, "Payment record of reservation with id $reservationId not found")
        }
    }

    override fun getPaymentRecordByToken(token: String): PaymentRecord {
        return paymentRecordRepository.findByToken(token).orElseThrow {
            FailureException(ResponseEnum.PAYMENT_NOT_FOUND, "Payment record not found with token $token")
        }
    }

    override fun getPaymentRecords(
        page: Int,
        size: Int,
        sortBy: String,
        sortOrder: String,
        @Valid filters: PaymentRecordFilter
    ): PagedResDTO<PaymentRecordResDTO> {
        var spec: Specification<PaymentRecord> = Specification.where(null)
        // Reservation id
        filters.reservationId?.let { reservationId ->
            spec = spec.and { root, _, cb ->
                cb.equal(root.get<Long>("reservationId"), reservationId)
            }
        }
        // Customer username
        filters.customerUsername?.let { username ->
            spec = spec.and { root, _, cb ->
                cb.equal(root.get<String>("customerUsername"), username)
            }
        }
        // Min amount
        filters.minAmount?.let { minAmount ->
            spec = spec.and { root, _, cb ->
                cb.greaterThanOrEqualTo(root.get("amount"), minAmount)
            }
        }
        // Max amount
        filters.maxAmount?.let { maxAmount ->
            spec = spec.and { root, _, cb ->
                cb.lessThanOrEqualTo(root.get("amount"), maxAmount)
            }
        }
        // Token
        filters.token?.takeIf { it.isNotBlank() }?.let { token ->
            spec = spec.and { root, _, cb ->
                cb.like(cb.lower(root.get("token")), "${token.lowercase()}%")
            }
        }
        // Status
        filters.status?.let { status ->
            spec = spec.and { root, _, cb ->
                cb.equal(root.get<PaymentRecordStatus>("status"), status)
            }
        }
        // Sorting
        val sortOrd: Sort.Direction = if (sortOrder == "asc") Sort.Direction.ASC else Sort.Direction.DESC
        val pageable: Pageable = PageRequest.of(page, size, sortOrd, sortBy)
        val pageResult = paymentRecordRepository.findAll(spec, pageable)

        return PagedResDTO(
            currentPage = pageResult.number,
            totalPages = pageResult.totalPages,
            totalElements = pageResult.totalElements,
            elementsInPage = pageResult.numberOfElements,
            content = pageResult.content.map { it.toResDTO() }
        )
    }
}

@Service
class UpdatePaymentServiceImpl(
    private val paymentRecordRepository: PaymentRecordRepository
) : UpdatePaymentService {
    @Transactional
    override fun setPaid(token: String) {
        paymentRecordRepository.findByToken(token).get().status = PaymentRecordStatus.PAID
    }

    @Transactional
    override fun setCompleted(token: String) {
        paymentRecordRepository.findByToken(token).get().status = PaymentRecordStatus.COMPLETED
    }

    @Transactional
    override fun setCancelled(token: String) {
        paymentRecordRepository.findByToken(token).get().status = PaymentRecordStatus.CANCELLED
    }
}