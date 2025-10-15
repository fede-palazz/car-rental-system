package com.rentalcarsystem.model

import java.time.Instant

data class UserEventPayload (
    val eventType: String,
    val userId: String,
    val username: String? = null,
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val timestamp: Long,
    val realmId: String,
    val clientId: String? = null,
    val ipAddress: String? = null,
    val sessionId: String? = null,
    val adminUserId: String? = null,
    val userAttributes: Map<String, String?>? = null,
    val details: Map<String, String>? = null,
    val createdAt: String = Instant.now().toString()
)