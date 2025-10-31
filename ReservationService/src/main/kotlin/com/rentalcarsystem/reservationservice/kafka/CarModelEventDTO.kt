package com.rentalcarsystem.reservationservice.kafka

import com.rentalcarsystem.reservationservice.dtos.response.CarModelResDTO
import com.rentalcarsystem.reservationservice.enums.EventType

data class CarModelEventDTO(
    val type: EventType,
    val carModel: CarModelResDTO? = null,  // Optional since it is unnecessary for deleted events
    val compositeId: String? = null  // Used for updated and deleted events to identify the car model in format: "brand,model,year"
)