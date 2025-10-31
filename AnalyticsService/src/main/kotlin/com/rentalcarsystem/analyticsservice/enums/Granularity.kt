package com.rentalcarsystem.analyticsservice.enums

enum class Granularity {
    DAY,
    MONTH,
    YEAR;

    companion object {
        private val granularityMap = mapOf(
            DAY to "day",
            MONTH to "month",
            YEAR to "year"
        )

        fun getValue(granularity: Granularity): String {
            return granularityMap.getValue(granularity)
        }
    }
}