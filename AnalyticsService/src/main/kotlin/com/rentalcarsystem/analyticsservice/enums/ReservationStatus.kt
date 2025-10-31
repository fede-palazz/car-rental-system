package com.rentalcarsystem.analyticsservice.enums

enum class ReservationStatus {
    PENDING,    // Customer created reservation, but it has not been paid yet
    EXPIRED,    // Customer did not pay the reservation within the allowed time frame
    CANCELLED,  // Customer cancelled the reservation before it was set as expired
    CONFIRMED,  // Customer successfully paid and reserved a vehicle
    PICKED_UP,  // Customer picked up the vehicle
    DELIVERED,  // Customer returned the vehicle
}