package com.rentalcarsystem.reservationservice.filters

import java.time.LocalDate

data class NoteFilter(
    val content: String? = null,
    val author: String? = null,
    val minDate: LocalDate? = null,
    val maxDate: LocalDate? = null,
)
