package com.rentalcarsystem.paymentservice.services

import com.paypal.sdk.PaypalServerSdkClient
import com.paypal.sdk.models.*
import com.rentalcarsystem.paymentservice.dtos.request.PaymentReqDTO
import com.rentalcarsystem.paymentservice.kafka.PayPalOrderListener
import com.rentalcarsystem.paymentservice.models.PayPalOutboxEvent
import com.rentalcarsystem.paymentservice.repositories.PayPalOutboxRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*


@Service
class PayPalService(
    private val payPalClient: PaypalServerSdkClient,
    @Value("\${paypal.currency}") private val currency: String,
    @Value("\${paypal.return-url}") private val returnUrl :String,
    @Value("\${paypal.cancel-url}") private val cancelUrl : String,
    private val outboxRepository: PayPalOutboxRepository
    ) {
    private val logger = LoggerFactory.getLogger(PayPalService::class.java)

    /**
     * Creates a PayPal order for reservation payment
     */
    fun createOrder(
        paymentInfo: PaymentReqDTO
    ): Order? {
        val orderRequest = OrderRequest().apply {
            intent = CheckoutPaymentIntent.CAPTURE
        }

        val item = Item().apply {
            name = paymentInfo.description
            unitAmount = Money().apply {
                currencyCode = currency
                value = String.format(Locale.US, "%.2f", paymentInfo.amount)
            }
            this.quantity = "1"
        }

        val amount = AmountWithBreakdown().apply {
            currencyCode = currency
            value = String.format(Locale.US, "%.2f", paymentInfo.amount)
            breakdown = AmountBreakdown().apply {
                itemTotal = Money().apply {
                    currencyCode = currency
                    value = String.format(Locale.US, "%.2f", paymentInfo.amount)
                }
            }
        }

        val purchaseUnitRequest = PurchaseUnitRequest().apply {
            this.amount = amount
            this.items = listOf(item)
        }

        orderRequest.purchaseUnits = listOf(purchaseUnitRequest)

        val applicationContext = OrderApplicationContext()
        applicationContext.returnUrl = returnUrl
        applicationContext.cancelUrl = cancelUrl

        orderRequest.applicationContext = applicationContext

        val createOrderInput = CreateOrderInput();
        createOrderInput.body = orderRequest;

        try {
            return payPalClient.ordersController.createOrder(createOrderInput).result
        } catch (e: Exception){
            logger.error(e.message, e)
            return null;
        }
    }

    /**
     * Captures payment for an approved order
     */
    fun captureOrder(token: String, payerId: String): Order {
        val paymentSource = OrderCaptureRequestPaymentSource()
        paymentSource.token=Token(token,TokenType.BILLING_AGREEMENT);

        val captureOrderRequest = OrderCaptureRequest()
        captureOrderRequest.paymentSource = paymentSource

        val captureOrderInput = CaptureOrderInput();
        captureOrderInput.body = captureOrderRequest;
        captureOrderInput.id = token

        return payPalClient.ordersController.captureOrder(captureOrderInput).result
    }

    @Transactional
    fun createPayPalCaptureEvent(paypalToken: String, payerId: String, paymentId: Long, reservationId: Long) {
        val event = PayPalOutboxEvent(
            paymentId,
            paypalToken,
            payerId,
            reservationId
        )
        val cdcOptimization = outboxRepository.save(event)
        outboxRepository.delete(cdcOptimization)
    }
}