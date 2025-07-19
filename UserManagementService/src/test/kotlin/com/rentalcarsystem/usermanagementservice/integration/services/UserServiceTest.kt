package com.rentalcarsystem.usermanagementservice.integration.services

import com.rentalcarsystem.usermanagementservice.dtos.request.UserReqDTO
import com.rentalcarsystem.usermanagementservice.dtos.request.UserUpdateReqDTO
import com.rentalcarsystem.usermanagementservice.exceptions.FailureException
import com.rentalcarsystem.usermanagementservice.filters.UserFilter
import com.rentalcarsystem.usermanagementservice.models.User
import com.rentalcarsystem.usermanagementservice.models.UserRole
import com.rentalcarsystem.usermanagementservice.repositories.UserRepository
import com.rentalcarsystem.usermanagementservice.services.UserService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class UserServiceImplTest {

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var savedUser: User

    @BeforeEach
    fun setUp() {
        val user = User(
            firstName = "John",
            lastName = "Doe",
            email = "123@gmail.com",
            role = UserRole.CUSTOMER,
            phone = "1234567890",
            address = "123 Main St"
        )
        savedUser = userRepository.save(user)
    }

    @AfterEach
    fun cleanUp() {
        userRepository.deleteAll()
    }

    @Test
    fun `should add user successfully`() {
        assertThat(savedUser.getId()).isNotNull()
        assertThat(savedUser.email).isEqualTo("123@gmail.com")
    }

    @Test
    fun `should get user by ID`() {
        val fetched = userService.getUserById(savedUser.getId()!!)
        assertThat(fetched.firstName).isEqualTo("John")
    }

    @Test
    fun `should throw when user not found`() {
        val ex = assertThrows(FailureException::class.java) {
            userService.getUserById(999L)
        }
        assertThat(ex.message).isEqualTo("User with id 999 not found")
    }

    @Test
    fun `should update user successfully`() {
        val update = UserUpdateReqDTO(
            firstName = "Jane",
            lastName = "Smith",
            phone = "9876543210",
            address = "456 New Rd",
            role = UserRole.STAFF,
            eligibilityScore = 85.0
        )
        val updated = userService.updateUser(savedUser.getId()!!, update)

        assertThat(updated.firstName).isEqualTo("Jane")
        assertThat(updated.role).isEqualTo(UserRole.STAFF)
    }

    @Test
    fun `should delete user successfully`() {
        val userId = savedUser.getId()!!
        userService.deleteUser(userId)
        val ex = assertThrows(FailureException::class.java) {
            userService.getUserById(userId)
        }
        assertThat(ex.message).isEqualTo("User with id $userId not found")
    }


    @Test
    fun `should throw when adding duplicate email`() {
        val duplicateReq = UserReqDTO(
            firstName = "Johnny",
            lastName = "Doe",
            email = "123@gmail.com",
            phone = "0001112222",
            address = "Dup Addr",
            role = UserRole.CUSTOMER
        )

        val ex = assertThrows(FailureException::class.java) {
            userService.addUser(duplicateReq)
        }
        assertThat(ex.message).isEqualTo("A user with email ${duplicateReq.email} already exists")
    }

    @Test
    fun `should filter users by role`() {
        val filter = UserFilter(role = UserRole.CUSTOMER)
        val result = userService.getUsers(0, 10, "id", "asc", filter)

        assertThat(result).isNotEmpty
        assertThat(result[0].role).isEqualTo(UserRole.CUSTOMER)
    }

    @Test
    fun `should return empty list when filter does not match`() {
        val filter = UserFilter(firstName = "NoMatchName")
        val result = userService.getUsers(0, 10, "id", "asc", filter)

        assertThat(result).isEmpty()
    }
}
