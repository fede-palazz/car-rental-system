package com.rentalcarsystem.usermanagementservice.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "user_preferences")
class UserPreferences(
    @OneToOne
    var user: User? = null,
    @Column(columnDefinition = "TEXT")
    var preferredSegments: String? = null,   // Comma separated list of car segments
    @Column(columnDefinition = "TEXT")
    val favoriteBrands: String? = null,     // Comma separated list of car brands
    @Column(columnDefinition = "TEXT")
    val favoriteFeatures: String? = null,   // Comma separated list of car features
) : BaseEntity<Long>()