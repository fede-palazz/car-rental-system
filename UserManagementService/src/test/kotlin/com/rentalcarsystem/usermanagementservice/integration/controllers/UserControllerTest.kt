package com.rentalcarsystem.usermanagementservice.integration.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.rentalcarsystem.usermanagementservice.dtos.request.UserReqDTO
import com.rentalcarsystem.usermanagementservice.models.User
import com.rentalcarsystem.usermanagementservice.models.UserRole
import com.rentalcarsystem.usermanagementservice.repositories.UserRepository
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)  // Allows us to use @BeforeAll
class UserControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    final val user = User(
        firstName = "John",
        lastName = "Doe",
        email = "123@gmail.com",
        role = UserRole.CUSTOMER,
        phone = "1234567890",
        address = "123 Main St"
    )

    @BeforeAll
    fun setup() {
        userRepository.save(user)
    }

    @AfterAll
    fun cleanup() {
        userRepository.deleteAll()
    }

    @Nested
    inner class CreateUserTests {

        @Nested
        inner class InvalidDataErrors {

            @Test
            fun `should return 422 for blank first name`() {
                val invalidUserReqDTO = UserReqDTO(
                    firstName = "",
                    lastName = "Invalid",
                    email = "valid.email@example.com",
                    role = UserRole.CUSTOMER,
                    phone = "1234567890",
                    address = "Test Address"
                )

                mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserReqDTO))
                ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
            }

            @Test
            fun `should return 422 for invalid email`() {
                val invalidUserReqDTO = UserReqDTO(
                    firstName = "Invalid",
                    lastName = "User",
                    email = "invalid-email",  // Invalid email format
                    role = UserRole.CUSTOMER,
                    phone = "1234567890",
                    address = "Test Address"
                )

                mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserReqDTO))
                ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
            }

            @Test
            fun `should return 422 for invalid phone number`() {
                val invalidUserReqDTO = UserReqDTO(
                    firstName = "Invalid",
                    lastName = "User",
                    email = "valid.email@example.com",
                    role = UserRole.CUSTOMER,
                    phone = "12345",
                    address = "Test Address"
                )

                mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserReqDTO))
                ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
            }
        }

        @Nested
        inner class ConflictErrors {

            @Test
            fun `should return 409 for duplicate email`() {
                val duplicateEmailUserReqDTO = UserReqDTO(
                    firstName = "Another",
                    lastName = "User",
                    email = "123@gmail.com",
                    role = UserRole.CUSTOMER,
                    phone = "1234567890",
                    address = "Test Address"
                )

                mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateEmailUserReqDTO))
                ).andExpect(MockMvcResultMatchers.status().isConflict)
            }
        }
    }

    @Nested
    inner class GetUserTests {

        @Test
        fun `should return 404 for non-existent user`() {
            val nonExistentId = 999999L

            mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/users/$nonExistentId")
            ).andExpect(MockMvcResultMatchers.status().isNotFound)
        }
    }

    @Nested
    inner class UpdateUserTests {

        @Test
        fun `should return 422 for invalid update data`() {
            val invalidUpdateDTO = UserReqDTO(
                firstName = "",
                lastName = "Updated",
                email = "updated.user@example.com",
                role = UserRole.CUSTOMER,
                phone = "1234567890",
                address = "Updated Address"
            )

            mockMvc.perform(
                MockMvcRequestBuilders.put("/api/v1/users/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidUpdateDTO))
            ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity)
        }
    }

    @Nested
    inner class DeleteUserTests {

        @Test
        fun `should return 404 for non-existent user deletion`() {
            val nonExistentId = 999999L

            mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/v1/users/$nonExistentId")
            ).andExpect(MockMvcResultMatchers.status().isNotFound)
        }
    }
}