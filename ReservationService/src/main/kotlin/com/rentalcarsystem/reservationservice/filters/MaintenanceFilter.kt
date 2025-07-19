package com.rentalcarsystem.reservationservice.filters

import java.time.LocalDate

data class MaintenanceFilter(
    val defects: String? = null,
    val completed: Boolean? = null,
    val type: String? = null,
    val upcomingServiceNeeds: String? = null,
    val minDate: LocalDate? = null,
    val maxDate: LocalDate? = null
)
