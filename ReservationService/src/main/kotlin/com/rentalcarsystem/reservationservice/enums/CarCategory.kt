package com.rentalcarsystem.reservationservice.enums

const val ECONOMY_THRESHOLD = 0.0
const val MIDSIZE_THRESHOLD = 40.0
const val FULLSIZE_THRESHOLD = 60.0
const val PREMIUM_THRESHOLD = 75.0
const val LUXURY_THRESHOLD = 90.0

enum class CarCategory {
    ECONOMY,
    MIDSIZE,
    FULLSIZE,
    PREMIUM,
    LUXURY;

    companion object {
        private val categoryThresholds = mapOf(
            ECONOMY to ECONOMY_THRESHOLD,
            MIDSIZE to MIDSIZE_THRESHOLD,
            FULLSIZE to FULLSIZE_THRESHOLD,
            PREMIUM to PREMIUM_THRESHOLD,
            LUXURY to LUXURY_THRESHOLD
        )

        fun getValue(category: CarCategory): Double? {
            return categoryThresholds[category]
        }
    }
}