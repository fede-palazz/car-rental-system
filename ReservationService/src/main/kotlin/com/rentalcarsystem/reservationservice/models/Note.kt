package com.rentalcarsystem.reservationservice.models

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "notes")
class Note(
    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String,

    @Column(nullable = false)
    var author: String,

    @Column(nullable = false)
    var date: LocalDateTime,

    // A Note belongs to one Vehicle only
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    var vehicle: Vehicle? = null,
) : BaseEntity<Long>()