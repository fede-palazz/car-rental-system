package com.rentalcarsystem.reservationservice.filters

import com.rentalcarsystem.reservationservice.enums.MaintenanceType
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate

data class MaintenanceFilter(
    val defects: String? = null,
    val completed: Boolean? = null,
    val type: MaintenanceType? = null,
    val upcomingServiceNeeds: String? = null,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val minStartDate: LocalDate? = null,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val maxStartDate: LocalDate? = null,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val minPlannedEndDate: LocalDate? = null,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val maxPlannedEndDate: LocalDate? = null,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val minActualEndDate: LocalDate? = null,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val maxActualEndDate: LocalDate? = null
)
