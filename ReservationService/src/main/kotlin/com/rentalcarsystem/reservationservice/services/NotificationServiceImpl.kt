package com.rentalcarsystem.reservationservice.services

import com.rentalcarsystem.reservationservice.config.EmailProperties
import com.rentalcarsystem.reservationservice.kafka.ReservationEventDTO
import jakarta.mail.internet.MimeMessage
import org.slf4j.LoggerFactory
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class NotificationServiceImpl(
    private val mailSender: JavaMailSender,
    private val templateEngine: TemplateEngine,
    private val emailProperties: EmailProperties
) : NotificationService {

    private val logger = LoggerFactory.getLogger(NotificationServiceImpl::class.java)
    private val LOCAL_ZONE = ZoneId.of("Europe/Rome")
    private val UTC = ZoneId.of("UTC")

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy 'at' HH:mm")
        private val DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    }

    override fun sendReservationConfirmedEmail(
        recipientEmail: String,
        recipientName: String,
        reservationEvent: ReservationEventDTO
    ) {
        val reservation = reservationEvent.reservation
        val context = Context().apply {
            setVariable("recipientName", recipientName)
            setVariable("reservationId", reservation.commonInfo.id)
            setVariable("brand", reservation.commonInfo.brand)
            setVariable("model", reservation.commonInfo.model)
            setVariable("year", reservation.commonInfo.year)
            setVariable("licensePlate", reservation.commonInfo.licensePlate)
            setVariable("pickUpDate", reservation.commonInfo.plannedPickUpDate
                .atZone(UTC)                     // interpret as UTC
                .withZoneSameInstant(LOCAL_ZONE)        // convert to local zone
                .format(DATE_FORMATTER)
            )
            setVariable("dropOffDate", reservation.commonInfo.plannedDropOffDate
                .atZone(UTC)                     // interpret as UTC
                .withZoneSameInstant(LOCAL_ZONE)        // convert to local zone
                .format(DATE_FORMATTER)
            )
            setVariable("totalAmount", String.format("%.2f", reservation.commonInfo.totalAmount))
            setVariable("creationDate", reservation.commonInfo.creationDate
                .atZone(UTC)                     // interpret as UTC
                .withZoneSameInstant(LOCAL_ZONE)        // convert to local zone
                .format(DATE_FORMATTER)
            )
        }

        val subject = "Reservation Confirmed - ${reservation.commonInfo.brand} ${reservation.commonInfo.model}"
        sendEmail(recipientEmail, subject, "reservation-created", context)
    }

    override fun sendReservationCancelledEmail(
        recipientEmail: String,
        recipientName: String,
        reservationEvent: ReservationEventDTO
    ) {
        val reservation = reservationEvent.reservation
        val context = Context().apply {
            setVariable("recipientName", recipientName)
            setVariable("reservationId", reservation.commonInfo.id)
            setVariable("brand", reservation.commonInfo.brand)
            setVariable("model", reservation.commonInfo.model)
            setVariable("year", reservation.commonInfo.year)
            setVariable("licensePlate", reservation.commonInfo.licensePlate)
            setVariable("pickUpDate", reservation.commonInfo.plannedPickUpDate
                .atZone(UTC)                     // interpret as UTC
                .withZoneSameInstant(LOCAL_ZONE)        // convert to local zone
                .format(DATE_FORMATTER)
            )
            setVariable("dropOffDate", reservation.commonInfo.plannedDropOffDate
                .atZone(UTC)                     // interpret as UTC
                .withZoneSameInstant(LOCAL_ZONE)        // convert to local zone
                .format(DATE_FORMATTER)
            )
            setVariable("totalAmount", String.format("%.2f", reservation.commonInfo.totalAmount))
            setVariable("creationDate", reservation.commonInfo.creationDate
                .atZone(UTC)                     // interpret as UTC
                .withZoneSameInstant(LOCAL_ZONE)        // convert to local zone
                .format(DATE_FORMATTER)
            )
        }

        val subject = "Reservation Cancelled - ${reservation.commonInfo.brand} ${reservation.commonInfo.model}"
        sendEmail(recipientEmail, subject, "reservation-cancelled", context)
    }

    override fun sendReservationModifiedEmail(
        recipientEmail: String,
        recipientName: String,
        reservationEvent: ReservationEventDTO
    ) {
        val reservation = reservationEvent.reservation
        val context = Context().apply {
            setVariable("recipientName", recipientName)
            setVariable("reservationId", reservation.commonInfo.id)
            setVariable("brand", reservation.commonInfo.brand)
            setVariable("model", reservation.commonInfo.model)
            setVariable("year", reservation.commonInfo.year)
            setVariable("licensePlate", reservation.commonInfo.licensePlate)
            setVariable("pickUpDate", reservation.commonInfo.plannedPickUpDate
                .atZone(UTC)                     // interpret as UTC
                .withZoneSameInstant(LOCAL_ZONE)        // convert to local zone
                .format(DATE_FORMATTER)
            )
            setVariable("dropOffDate", reservation.commonInfo.plannedDropOffDate
                .atZone(UTC)                     // interpret as UTC
                .withZoneSameInstant(LOCAL_ZONE)        // convert to local zone
                .format(DATE_FORMATTER)
            )
            setVariable("totalAmount", String.format("%.2f", reservation.commonInfo.totalAmount))
            setVariable("status", reservation.commonInfo.status.toString())
        }

        val subject = "Reservation Updated - ${reservation.commonInfo.brand} ${reservation.commonInfo.model}"
        sendEmail(recipientEmail, subject, "reservation-modified", context)
    }

    override fun sendVehiclePickedUpEmail(
        recipientEmail: String,
        recipientName: String,
        reservationEvent: ReservationEventDTO
    ) {
        val reservation = reservationEvent.reservation
        val context = Context().apply {
            setVariable("recipientName", recipientName)
            setVariable("reservationId", reservation.commonInfo.id)
            setVariable("brand", reservation.commonInfo.brand)
            setVariable("model", reservation.commonInfo.model)
            setVariable("year", reservation.commonInfo.year)
            setVariable("licensePlate", reservation.commonInfo.licensePlate)
            setVariable("vin", reservation.commonInfo.vin)
            setVariable("pickUpDate", reservation.commonInfo.actualPickUpDate?.atZone(UTC)?.withZoneSameInstant(LOCAL_ZONE)    ?.format(DATE_FORMATTER) ?: "N/A")
            setVariable("plannedDropOffDate", reservation.commonInfo.plannedDropOffDate
                .atZone(UTC)                     // interpret as UTC
                .withZoneSameInstant(LOCAL_ZONE)        // convert to local zone
                .format(DATE_FORMATTER)
            )
            setVariable("dropOffDateOnly", reservation.commonInfo.plannedDropOffDate
                .atZone(UTC)                     // interpret as UTC
                .withZoneSameInstant(LOCAL_ZONE)        // convert to local zone
                .format(DATE_FORMATTER)
            )
            setVariable("totalAmount", String.format("%.2f", reservation.commonInfo.totalAmount))
            setVariable("pickUpStaff", reservation.pickUpStaffUsername ?: "Staff Member")
        }

        val subject = "Vehicle Picked Up - ${reservation.commonInfo.brand} ${reservation.commonInfo.model}"
        sendEmail(recipientEmail, subject, "vehicle-picked-up", context)
    }

    override fun sendVehicleDroppedOffEmail(
        recipientEmail: String,
        recipientName: String,
        reservationEvent: ReservationEventDTO
    ) {
        val reservation = reservationEvent.reservation
        val wasLate = reservation.wasDeliveryLate ?: false
        val wasCharged = reservation.wasChargedFee ?: false
        val hadAccident = reservation.wasInvolvedInAccident ?: false

        val context = Context().apply {
            setVariable("recipientName", recipientName)
            setVariable("reservationId", reservation.commonInfo.id)
            setVariable("brand", reservation.commonInfo.brand)
            setVariable("model", reservation.commonInfo.model)
            setVariable("year", reservation.commonInfo.year)
            setVariable("licensePlate", reservation.commonInfo.licensePlate)
            setVariable("vin", reservation.commonInfo.vin)
            setVariable("pickUpDate", reservation.commonInfo.actualPickUpDate?.format(DATE_FORMATTER) ?: "N/A")
            setVariable("plannedDropOffDate", reservation.commonInfo.plannedDropOffDate.format(DATE_FORMATTER))
            setVariable("actualDropOffDate", reservation.commonInfo.actualDropOffDate?.format(DATE_FORMATTER) ?: "N/A")
            setVariable("totalAmount", String.format("%.2f", reservation.commonInfo.totalAmount))
            setVariable("dropOffStaff", reservation.dropOffStaffUsername ?: "Staff Member")
            setVariable("wasLate", wasLate)
            setVariable("wasChargedFee", wasCharged)
            setVariable("wasInvolvedInAccident", hadAccident)
            setVariable("damageLevel", reservation.damageLevel ?: 0)
            setVariable("dirtinessLevel", reservation.dirtinessLevel ?: 0)
        }

        val subject = "Vehicle Returned - ${reservation.commonInfo.brand} ${reservation.commonInfo.model}"
        sendEmail(recipientEmail, subject, "vehicle-dropped-off", context)
    }

    private fun sendEmail(
        to: String,
        subject: String,
        templateName: String,
        context: Context
    ) {
        try {
            val mimeMessage: MimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(mimeMessage, true, "UTF-8")

            helper.setFrom(emailProperties.from, emailProperties.fromName)
            helper.setTo(to)
            helper.setSubject(subject)

            val htmlContent = templateEngine.process(templateName, context)
            helper.setText(htmlContent, true)

            mailSender.send(mimeMessage)
            logger.info("Sent email to $to with subject $subject")
        } catch (e: Exception) {
            throw RuntimeException("Failed to send email to $to", e)
        }
    }
}