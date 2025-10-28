package com.rentalcarsystem.reservationservice.kafka

import com.rentalcarsystem.reservationservice.dtos.response.VehicleResDTO
import com.rentalcarsystem.reservationservice.enums.EventType

data class VehicleEventDTO(
    val type: EventType,
    val vehicles: List<VehicleResDTO>
)
