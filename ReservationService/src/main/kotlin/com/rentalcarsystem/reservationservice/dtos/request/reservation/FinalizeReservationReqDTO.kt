package com.rentalcarsystem.reservationservice.dtos.request.reservation

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.rentalcarsystem.reservationservice.utils.CustomBooleanDeserializer
import jakarta.validation.constraints.PastOrPresent
import java.time.LocalDateTime

data class FinalizeReservationReqDTO(
    // @field:PastOrPresent(message = "Parameter 'actualDropOffDate' must be a past or present date") TODO: Uncomment this line after testing
    val actualDropOffDate: LocalDateTime,
    @field:JsonDeserialize(using = CustomBooleanDeserializer::class)
    val wasDeliveryLate: Boolean? = false,
    @field:JsonDeserialize(using = CustomBooleanDeserializer::class)
    val wasChargedFee: Boolean? = false,
    @field:JsonDeserialize(using = CustomBooleanDeserializer::class)
    val wasVehicleDamaged: Boolean? = false,
    @field:JsonDeserialize(using = CustomBooleanDeserializer::class)
    val wasInvolvedInAccident: Boolean? = false
)
