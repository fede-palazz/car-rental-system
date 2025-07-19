package com.rentalcarsystem.usermanagementservice.models

import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
    @Column(unique = true, nullable = false)
    var username: String,

    @Column(nullable = false)
    var firstName: String,

    @Column(nullable = false)
    var lastName: String,

    @Column(unique = true, nullable = false)
    var email: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: UserRole,

    @Column(nullable = false)
    var phone: String,

    @Column(nullable = false)
    var address: String,

    var eligibilityScore: Double? = null,   // Not null only for customers

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var preferences: UserPreferences? = null,
) : BaseEntity<Long>()


enum class UserRole {
    CUSTOMER,
    STAFF,
    FLEET_MANAGER,
    MANAGER
}
