package com.rentalcarsystem.usermanagementservice.services

import com.rentalcarsystem.usermanagementservice.dtos.request.UserReqDTO
import com.rentalcarsystem.usermanagementservice.dtos.request.UserUpdateReqDTO
import com.rentalcarsystem.usermanagementservice.dtos.response.UserResDTO
import com.rentalcarsystem.usermanagementservice.filters.UserFilter
import com.rentalcarsystem.usermanagementservice.models.User
import jakarta.validation.Valid

interface UserService {
    fun getUsers(
        page: Int,
        size: Int,
        sortBy: String,
        sortOrder: String,
        @Valid filters: UserFilter,
    ): List<UserResDTO>

    fun getUserById(userId: Long): UserResDTO
    fun getUserByUsername(username: String, loggedUsername: String, roles: List<String>): UserResDTO
    fun getActualUserById(userId: Long): User
    fun addUser(@Valid userReq: UserReqDTO): UserResDTO
    fun updateUser(userId: Long, @Valid userReq: UserUpdateReqDTO): UserResDTO
    fun deleteUser(userId: Long)
}
