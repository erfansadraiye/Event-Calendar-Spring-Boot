package com.example.eventcalendar.user

import com.example.eventcalendar.CalendarTest
import com.example.eventcalendar.calendar.Task
import com.example.eventcalendar.calendar.TaskState
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.json.JSONArray
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.time.LocalDate

internal class UserServiceTest : CalendarTest {

    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    val mapper = jacksonObjectMapper()


    final val ERFAN_FIRST_NAME: String = "erfan"
    final val ERFAN_LAST_NAME: String = "sadraiye"
    final val ERFAN_EMAIL: String = "erfan@gmail.com"

    final val DANIAL_FIRST_NAME: String = "danial"
    final val DANIAL_LAST_NAME: String = "jahanbani"
    final val DANIAL_EMAIL: String = "danial@gmail.com"

    final val pathUsers = "http://localhost:8080/api/calendar/users/"

    @BeforeEach
    override fun setupMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    @BeforeEach
    override fun resetDB() {
        userRepository.deleteAll()
    }

    @Test
    fun getAllUsers() {
        assertTrue(userRepository.count() == 0L)
        val erfan = User(ERFAN_FIRST_NAME, ERFAN_LAST_NAME, ERFAN_EMAIL)
        val danial = User(DANIAL_FIRST_NAME, DANIAL_LAST_NAME, DANIAL_EMAIL)
        userRepository.saveAll(listOf(erfan, danial))
        assertTrue(userRepository.count() == 2L)
        val actual_tasks = mockMvc.perform(
            MockMvcRequestBuilders.get("${pathUsers}list")
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn().response.contentAsString
        val actual_list = JSONArray(actual_tasks)
        assertEquals(actual_list.length(), 2)
        val actualErfanObject = mapper.readValue<User>(actual_list.getString(0))
        val actualDanialObject = mapper.readValue<User>(actual_list.getString(1))
        assertEquals(erfan, actualErfanObject)
        assertEquals(danial, actualDanialObject)
    }

    @Test
    fun getUserById() {
        val erfan = User(ERFAN_FIRST_NAME, ERFAN_LAST_NAME, ERFAN_EMAIL)
        userRepository.save(erfan)
        mockMvc.perform(
            MockMvcRequestBuilders.get("${pathUsers}id/${(erfan.id!! + 10)}")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)

        val actual = mockMvc.perform(
            MockMvcRequestBuilders.get("${pathUsers}id/${(erfan.id)}")
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn().response.contentAsString
        assertEquals(erfan, mapper.readValue<User>(actual))
    }

    @Test
    fun getUserByEmail() {
        val erfan = User(ERFAN_FIRST_NAME, ERFAN_LAST_NAME, ERFAN_EMAIL)
        userRepository.save(erfan)
        mockMvc.perform(
            MockMvcRequestBuilders.get("${pathUsers}email/random_email")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)

        val actual = mockMvc.perform(
            MockMvcRequestBuilders.get("${pathUsers}email/${(erfan.email)}")
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn().response.contentAsString
        assertEquals(erfan, mapper.readValue<User>(actual))
    }

    @Test
    fun addUser() {
        val jsonErfan: String = toJsonUser(ERFAN_FIRST_NAME, ERFAN_LAST_NAME, ERFAN_EMAIL)
        val actual = mockMvc.perform(
            MockMvcRequestBuilders.post("${pathUsers}create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonErfan)
        ).andExpect(MockMvcResultMatchers.status().isCreated).andReturn().response.contentAsString
        val expected = userRepository.findByEmail(ERFAN_EMAIL).get()
        assertEquals(actual.toLong(), expected.id)
        mockMvc.perform(
            MockMvcRequestBuilders.post("${pathUsers}create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJsonUser(actual.toLong(), ERFAN_FIRST_NAME, ERFAN_LAST_NAME, ERFAN_EMAIL))
        ).andExpect(MockMvcResultMatchers.status().isConflict)
        mockMvc.perform(
            MockMvcRequestBuilders.post("${pathUsers}create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJsonUser(DANIAL_FIRST_NAME, DANIAL_LAST_NAME, ERFAN_EMAIL))
        ).andExpect(MockMvcResultMatchers.status().isConflict)
    }

    @Test
    fun updateEmailById() {
        val erfan = User(ERFAN_FIRST_NAME, ERFAN_LAST_NAME, ERFAN_EMAIL)
        val danial = User(DANIAL_FIRST_NAME, DANIAL_LAST_NAME, DANIAL_EMAIL)
        userRepository.saveAll(listOf(erfan, danial))
        val newEmail = "new@gmail.com"
        mockMvc.perform(
            MockMvcRequestBuilders.put("${pathUsers}change_email/${erfan.id!! + 100}?email=$newEmail")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
        mockMvc.perform(
            MockMvcRequestBuilders.put("${pathUsers}change_email/${erfan.id}?email=${danial.email}")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)

        val actual = mockMvc.perform(
            MockMvcRequestBuilders.put("${pathUsers}change_email/${erfan.id}?email=$newEmail")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn().response.contentAsString
        val actualObject = mapper.readValue<User>(actual)
        val expected = userRepository.findById(erfan.id!!).get()
        assertEquals(actualObject,expected)
        assertEquals(actualObject.email, newEmail)
    }

    @Test
    fun updateNameById() {
        val erfan = User(ERFAN_FIRST_NAME, ERFAN_LAST_NAME, ERFAN_EMAIL)
        val danial = User(DANIAL_FIRST_NAME, DANIAL_LAST_NAME, DANIAL_EMAIL)
        userRepository.saveAll(listOf(erfan,danial))
        val newFirstname = "erf"
        val newLastname = "sad"
        mockMvc.perform(
            MockMvcRequestBuilders.put("${pathUsers}change_name/${erfan.id!! + 100}?firstname=$newFirstname")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
        mockMvc.perform(
            MockMvcRequestBuilders.put("${pathUsers}change_name/${erfan.id}")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk)
        var actual = mockMvc.perform(
            MockMvcRequestBuilders.put("${pathUsers}change_name/${erfan.id}?firstname=$newFirstname")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn().response.contentAsString
        var actualObject = mapper.readValue<User>(actual)
        var expected = userRepository.findById(erfan.id!!).get()
        assertEquals(actualObject,expected)
        assertEquals(actualObject.firstName, newFirstname)

        actual = mockMvc.perform(
            MockMvcRequestBuilders.put("${pathUsers}change_name/${erfan.id}?lastname=$newLastname")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn().response.contentAsString
        actualObject = mapper.readValue<User>(actual)
        expected = userRepository.findById(erfan.id!!).get()
        assertEquals(actualObject,expected)
        assertEquals(actualObject.lastName, newLastname)

        actual = mockMvc.perform(
            MockMvcRequestBuilders.put("${pathUsers}change_name/${erfan.id}?firstname=${ERFAN_FIRST_NAME}&lastname=${ERFAN_LAST_NAME}")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn().response.contentAsString
        actualObject = mapper.readValue<User>(actual)
        expected = userRepository.findById(erfan.id!!).get()
        assertEquals(actualObject,expected)
        assertEquals(actualObject.firstName,ERFAN_FIRST_NAME)
        assertEquals(actualObject.lastName, ERFAN_LAST_NAME)
    }

    @Test
    fun deleteUserById() {
        val erfan = User(ERFAN_FIRST_NAME, ERFAN_LAST_NAME, ERFAN_EMAIL)
        userRepository.save(erfan)
        mockMvc.perform(
            MockMvcRequestBuilders.delete("${pathUsers}remove_user/${erfan.id!! + 1000}")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)

        mockMvc.perform(
            MockMvcRequestBuilders.delete("${pathUsers}remove_user/${(erfan.id)}")
        ).andExpect(MockMvcResultMatchers.status().isOk)
        assertFalse(userRepository.existsById(erfan.id!!))
        assertEquals(userRepository.count(), 0)
    }

    final fun toJsonUser(firstName: String?, lastName: String?, email: String?): String {
        return "{  \"firstName\": \"$firstName\",  \"lastName\": \"$lastName\",  \"email\": \"$email\"}"
    }

    final fun toJsonUser(id: Long?, firstName: String?, lastName: String?, email: String?): String {
        return "{ \"id\" : \"$id\",  \"firstName\": \"$firstName\",  \"lastName\": \"$lastName\",  \"email\": \"$email\"}"
    }
}