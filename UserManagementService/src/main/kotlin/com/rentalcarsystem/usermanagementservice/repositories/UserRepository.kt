package com.rentalcarsystem.usermanagementservice.repositories

import com.rentalcarsystem.usermanagementservice.models.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    fun existsByEmail(email: String): Boolean
    fun findByUsername(username: String): Optional<User>
}
