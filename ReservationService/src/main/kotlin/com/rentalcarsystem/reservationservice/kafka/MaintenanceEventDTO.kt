package com.rentalcarsystem.reservationservice.kafka

import com.rentalcarsystem.reservationservice.dtos.response.MaintenanceResDTO
import com.rentalcarsystem.reservationservice.enums.EventType

data class MaintenanceEventDTO(
    val type: EventType,
    val maintenance: MaintenanceResDTO,
    val startFleetManagerUsername: String? = null,  // Optional since it is unnecessary for deleted events
    val endFleetManagerUsername: String? = null,  // Optional since it is unnecessary for deleted events
)