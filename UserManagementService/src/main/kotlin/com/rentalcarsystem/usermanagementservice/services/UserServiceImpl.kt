package com.rentalcarsystem.usermanagementservice.services

import com.rentalcarsystem.usermanagementservice.dtos.request.UserReqDTO
import com.rentalcarsystem.usermanagementservice.dtos.request.UserUpdateReqDTO
import com.rentalcarsystem.usermanagementservice.dtos.request.toEntity
import com.rentalcarsystem.usermanagementservice.dtos.response.UserResDTO
import com.rentalcarsystem.usermanagementservice.dtos.response.toResDTO
import com.rentalcarsystem.usermanagementservice.exceptions.FailureException
import com.rentalcarsystem.usermanagementservice.exceptions.ResponseEnum
import com.rentalcarsystem.usermanagementservice.filters.UserFilter
import com.rentalcarsystem.usermanagementservice.models.User
import com.rentalcarsystem.usermanagementservice.models.UserRole
import com.rentalcarsystem.usermanagementservice.repositories.UserRepository
import jakarta.persistence.criteria.Predicate
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated

@Service
@Validated
@Transactional
class UserServiceImpl(
    private val userRepository: UserRepository
) : UserService {

    override fun getUsers(
        page: Int,
        size: Int,
        sortBy: String,
        sortOrder: String,
        @Valid filters: UserFilter
    ): List<UserResDTO> {
        val spec = Specification<User> { root, _, cb ->
            val predicates = mutableListOf<Predicate>()
            filters.username?.takeIf { it.isNotBlank() }?.let {
                predicates += cb.like(cb.lower(root.get("username")), "${it.trim().lowercase()}%")
            }
            filters.firstName?.takeIf { it.isNotBlank() }?.let {
                predicates += cb.like(cb.lower(root.get("firstName")), "${it.trim().lowercase()}%")
            }
            filters.lastName?.takeIf { it.isNotBlank() }?.let {
                predicates += cb.like(cb.lower(root.get("lastName")), "${it.trim().lowercase()}%")
            }
            filters.email?.takeIf { it.isNotBlank() }?.let {
                predicates += cb.like(cb.lower(root.get("email")), "${it.trim().lowercase()}%")
            }
            filters.role?.let {
                predicates += cb.equal(root.get<UserRole>("role"), it)
            }
            cb.and(*predicates.toTypedArray())
        }
        val sortDir = if (sortOrder.lowercase() == "asc") Sort.Direction.ASC else Sort.Direction.DESC
        val pageable: Pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy))
        return userRepository.findAll(spec, pageable).content.map { it.toResDTO() }
    }

    override fun getUserById(userId: Long): UserResDTO {
        return getActualUserById(userId).toResDTO()
    }

    override fun getUserByUsername(username: String, loggedUsername: String, roles: List<String>): UserResDTO {
        val isCustomer = roles.any { it == "CUSTOMER" }
        if (isCustomer && username != loggedUsername) {
            throw FailureException(ResponseEnum.FORBIDDEN, "You are not allowed to access information of another user")
        }
        return userRepository.findByUsername(username).orElseThrow {
            FailureException(ResponseEnum.USER_NOT_FOUND, "User with username $username not found")
        }.toResDTO()
    }

    override fun getActualUserById(userId: Long): User {
        return userRepository.findById(userId).orElseThrow {
            FailureException(ResponseEnum.USER_NOT_FOUND, "User with id $userId not found")
        }
    }

    override fun getActualUserByUsername(username: String): User {
        return userRepository.findByUsername(username).orElseThrow {
            FailureException(ResponseEnum.USER_NOT_FOUND, "User with username $username not found")
        }
    }

    override fun addUser(@Valid userReq: UserReqDTO): UserResDTO {
        if (userRepository.existsByEmail(userReq.email)) {
            throw FailureException(ResponseEnum.USER_DUPLICATED, "A user with email ${userReq.email} already exists")
        }
        return userRepository.save(userReq.toEntity()).toResDTO()
    }

    override fun updateUser(userId: Long, @Valid userReq: UserUpdateReqDTO): UserResDTO {
        val user = getActualUserById(userId)
        user.firstName = userReq.firstName
        user.lastName = userReq.lastName
        user.phone = userReq.phone
        user.address = userReq.address
        user.role = userReq.role ?: user.role
        user.eligibilityScore = userReq.eligibilityScore ?: user.eligibilityScore
        return user.toResDTO()
    }

    override fun deleteUser(userId: Long) {
        if (!userRepository.existsById(userId)) {
            throw FailureException(ResponseEnum.USER_NOT_FOUND, "User with id $userId not found")
        }
        userRepository.deleteById(userId)
    }

}
