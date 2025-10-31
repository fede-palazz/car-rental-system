package com.rentalcarsystem.reservationservice.dtos.request.reservation

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.rentalcarsystem.reservationservice.utils.CustomBooleanDeserializer
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.PastOrPresent
import java.time.LocalDateTime

data class FinalizeReservationReqDTO(
    // @field:PastOrPresent(message = "Parameter 'actualDropOffDate' must be a past or present date") TODO: Uncomment this line after testing
    val actualDropOffDate: LocalDateTime,
    val bufferedDropOffDate: LocalDateTime,
    @field:JsonDeserialize(using = CustomBooleanDeserializer::class)
    val wasDeliveryLate: Boolean? = false,
    @field:JsonDeserialize(using = CustomBooleanDeserializer::class)
    val wasChargedFee: Boolean? = false,
    @field:JsonDeserialize(using = CustomBooleanDeserializer::class)
    val wasInvolvedInAccident: Boolean? = false,
    @field:Max(5, message = "Parameter 'damageLevel' must be between 0 and 5")
    @field:Min(0, message = "Parameter 'damageLevel' must be between 0 and 5")
    val damageLevel: Int = 0,
    @field:Max(5, message = "Parameter 'dirtinessLevel' must be between 0 and 5")
    @field:Min(0, message = "Parameter 'dirtinessLevel' must be between 0 and 5")
    val dirtinessLevel: Int = 0
)
