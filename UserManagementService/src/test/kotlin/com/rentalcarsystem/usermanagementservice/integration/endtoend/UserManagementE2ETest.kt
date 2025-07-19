package com.rentalcarsystem.usermanagementservice.integration.endtoend

import com.fasterxml.jackson.databind.ObjectMapper
import com.rentalcarsystem.usermanagementservice.dtos.request.UserReqDTO
import com.rentalcarsystem.usermanagementservice.dtos.request.UserUpdateReqDTO
import com.rentalcarsystem.usermanagementservice.models.UserRole
import com.rentalcarsystem.usermanagementservice.repositories.UserRepository
import org.junit.jupiter.api.BeforeEach
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
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserManagementE2ETest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private var createdUserId: Long? = null

    private val BASE_URL = "/api/v1/users"
    @Autowired
    lateinit var userRepository: UserRepository

    @BeforeEach
    fun setup() {
        userRepository.deleteAll()
    }

    @Test
    fun `should create, fetch, update and delete user end-to-end`() {
        // 1. Create user
        val createRequest = UserReqDTO(
            firstName = "Alice",
            lastName = "Johnson",
            email = "alice_${System.currentTimeMillis()}@example.com",
            phone = "5551234567",
            address = "123 Apple St",
            role = UserRole.CUSTOMER,
        )


        val postResult = mockMvc.perform(
            MockMvcRequestBuilders.post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(createRequest.email))
            .andReturn()

        val responseJson = postResult.response.contentAsString
        val userId = objectMapper.readTree(responseJson).get("id").asLong()
        createdUserId = userId

        // 2. Fetch user by ID
        mockMvc.perform(MockMvcRequestBuilders.get("$BASE_URL/$userId"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("Alice"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.role").value("CUSTOMER"))

        // 3. Update user
        val updateRequest = UserUpdateReqDTO(
            firstName = "AliceUpdated",
            lastName = "JohnsonUpdated",
            phone = "9999999999",
            address = "456 Orange Rd",
            role = UserRole.STAFF,
            eligibilityScore = 90.0
        )

        mockMvc.perform(
            MockMvcRequestBuilders.put("$BASE_URL/$userId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("AliceUpdated"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.role").value("STAFF"))

        // 4. Delete user
        mockMvc.perform(MockMvcRequestBuilders.delete("$BASE_URL/$userId"))
            .andExpect(MockMvcResultMatchers.status().isNoContent)

        // 5. Verify user no longer exists
        mockMvc.perform(MockMvcRequestBuilders.get("$BASE_URL/$userId"))
            .andExpect(MockMvcResultMatchers.status().is4xxClientError)
    }
}