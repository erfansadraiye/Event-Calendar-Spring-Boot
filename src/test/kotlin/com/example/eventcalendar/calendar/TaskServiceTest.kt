package com.example.eventcalendar.calendar

import com.example.eventcalendar.CalendarTest
import com.example.eventcalendar.model.Task
import com.example.eventcalendar.model.TaskState
import com.example.eventcalendar.model.User
import com.example.eventcalendar.repository.EventCalendarRepository
import com.example.eventcalendar.repository.UserRepository
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.json.JSONArray
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.time.LocalDate

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = ["classpath:application-test.properties"])
internal class TaskServiceTest(

) : CalendarTest {

    lateinit var mockMvc: MockMvc


    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var eventCalendarRepository: EventCalendarRepository

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    val mapper = jacksonObjectMapper()

    final val TITLE_LOR: String = "LOR"
    final val DESCRIPTION_LOR: String = "watch $TITLE_LOR"
    final val DEADLINE_LOR: String = "2022-12-09"
    final val TITLE_GOT: String = "GOT"
    final val DESCRIPTION_GOT: String = "watch $TITLE_GOT"
    final val DEADLINE_GOT: String = "2023-01-13"

    final val ERFAN_FIRST_NAME: String = "erfan"
    final val ERFAN_LAST_NAME: String = "sadraiye"
    final val ERFAN_EMAIL: String = "erfan@gmail.com"

    final val DANIAL_FIRST_NAME: String = "danial"
    final val DANIAL_LAST_NAME: String = "jahanbani"
    final val DANIAL_EMAIL: String = "danial@gmail.com"

    final val pathTasks = "http://localhost:8080/api/calendar/tasks/"
    @BeforeEach
    override fun setupMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    @BeforeEach
    override fun resetDB() {
        userRepository.deleteAll()
        eventCalendarRepository.deleteAll()
    }

    @Test
    fun getAllTasks() {
        assertTrue(eventCalendarRepository.count() == 0L)
        val lor = Task(TITLE_LOR, DESCRIPTION_LOR, LocalDate.parse(DEADLINE_LOR))
        val got = Task(TITLE_GOT, DESCRIPTION_GOT, LocalDate.parse(DEADLINE_GOT))
        eventCalendarRepository.save(lor)
        eventCalendarRepository.save(got)
        assertTrue(eventCalendarRepository.count() == 2L)
        val actual_tasks = mockMvc.perform(
            MockMvcRequestBuilders.get("${pathTasks}list")
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn().response.contentAsString
        val actual_list = JSONArray(actual_tasks)
        assertEquals(actual_list.length(), 2)
        val actualTaskLOR = mapper.readValue<Task>(actual_list.getString(0))
        val actualTaskGOT = mapper.readValue<Task>(actual_list.getString(1))
        assertEquals(lor, actualTaskLOR)
        assertEquals(got, actualTaskGOT)
    }

    @Test
    fun getTaskById() {
        val lor = Task(TITLE_LOR, DESCRIPTION_LOR, LocalDate.parse(DEADLINE_LOR))
        eventCalendarRepository.save(lor)
        mockMvc.perform(
            MockMvcRequestBuilders.get("${pathTasks}id/${(lor.id!! + 10)}")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)

        val actual = mockMvc.perform(
            MockMvcRequestBuilders.get("${pathTasks}id/${(lor.id)}")
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn().response.contentAsString
        assertEquals(lor, mapper.readValue<Task>(actual))
    }

    @Test
    fun getTaskByTitle() {
        val lor = Task(TITLE_LOR, DESCRIPTION_LOR, LocalDate.parse(DEADLINE_LOR))
        eventCalendarRepository.save(lor)
        mockMvc.perform(
            MockMvcRequestBuilders.get("${pathTasks}title/random_title")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)

        val actual = mockMvc.perform(
            MockMvcRequestBuilders.get("${pathTasks}title/${(lor.title)}")
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn().response.contentAsString
        assertEquals(lor, mapper.readValue<Task>(actual))
    }

    @Test
    fun getTasksBySearchTitle() {
        val lor1 = Task(TITLE_LOR, DESCRIPTION_LOR, LocalDate.parse(DEADLINE_LOR))
        eventCalendarRepository.save(lor1)
        val lor2 = Task("berim $TITLE_LOR bogholim", DESCRIPTION_LOR, LocalDate.parse("2022-12-29"))
        eventCalendarRepository.save(lor2)
        val got = Task(TITLE_GOT, DESCRIPTION_GOT, LocalDate.parse(DEADLINE_GOT))
        eventCalendarRepository.save(got)
        val actual = mockMvc.perform(
            MockMvcRequestBuilders.get("${pathTasks}search_title/$TITLE_LOR")
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn().response.contentAsString
        val actualList = JSONArray(actual)
        assertEquals(actualList.length(), 2)
        assertEquals(lor1, mapper.readValue<Task>(actualList.getString(0)))
        assertEquals(lor2, mapper.readValue<Task>(actualList.getString(1)))
    }

    @Test
    fun getTasksByState() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("${pathTasks}state/random_state")
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)

        val lor1 = Task(TITLE_LOR, DESCRIPTION_LOR, LocalDate.parse(DEADLINE_LOR))
        eventCalendarRepository.save(lor1)
        val lor2 = Task(TITLE_LOR + "2", DESCRIPTION_LOR, LocalDate.parse(DEADLINE_LOR))
        lor2.state = TaskState.DOING
        eventCalendarRepository.save(lor2)
        val got = Task(TITLE_GOT, DESCRIPTION_GOT, LocalDate.parse(DEADLINE_GOT))
        eventCalendarRepository.save(got)
        val actual = mockMvc.perform(
            MockMvcRequestBuilders.get("${pathTasks}state/TO_DO")
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn().response.contentAsString
        val actualList = JSONArray(actual)
        assertEquals(actualList.length(), 2)
        assertEquals(lor1, mapper.readValue<Task>(actualList.getString(0)))
        assertEquals(got, mapper.readValue<Task>(actualList.getString(1)))

        val actual2 = mockMvc.perform(
            MockMvcRequestBuilders.get("${pathTasks}state/DOING")
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn().response.contentAsString
        val actualListDoing = JSONArray(actual2)
        assertEquals(actualListDoing.length(), 1)
        assertEquals(lor2, mapper.readValue<Task>(actualListDoing.getString(0)))
    }

    @Test
    fun getTaskUntilDeadline() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("${pathTasks}deadline/2022-13-29")
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)
        val lor1 = Task(TITLE_LOR, DESCRIPTION_LOR, LocalDate.parse(DEADLINE_LOR))
        eventCalendarRepository.save(lor1)
        val lor2 = Task(TITLE_LOR + "2", DESCRIPTION_LOR, LocalDate.parse("2022-12-29"))
        eventCalendarRepository.save(lor2)
        val got = Task(TITLE_GOT, DESCRIPTION_GOT, LocalDate.parse(DEADLINE_GOT))
        eventCalendarRepository.save(got)
        val actual = mockMvc.perform(
            MockMvcRequestBuilders.get("${pathTasks}deadline/2022-12-29")
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn().response.contentAsString
        val actualList = JSONArray(actual)
        assertEquals(actualList.length(), 2)
        assertEquals(lor1, mapper.readValue<Task>(actualList.getString(0)))
        assertEquals(lor2, mapper.readValue<Task>(actualList.getString(1)))
    }

    @Test
    fun addTask() {
        val json_lor: String = toJsonTask(TITLE_LOR, DESCRIPTION_LOR, DEADLINE_LOR)
        val actual = mockMvc.perform(
            MockMvcRequestBuilders.post("${pathTasks}add_task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json_lor)
        ).andExpect(MockMvcResultMatchers.status().isCreated).andReturn().response.contentAsString
        val expected = eventCalendarRepository.findTaskByTitleEquals(TITLE_LOR).get()
        assertEquals(actual.toLong(), expected.id)
        mockMvc.perform(
            MockMvcRequestBuilders.post("${pathTasks}add_task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJsonTask(actual.toLong(), TITLE_LOR, DESCRIPTION_LOR, DEADLINE_LOR))
        ).andExpect(MockMvcResultMatchers.status().isConflict)
        mockMvc.perform(
            MockMvcRequestBuilders.post("${pathTasks}add_task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJsonTask(TITLE_LOR, DESCRIPTION_LOR + "1", DEADLINE_LOR))
        ).andExpect(MockMvcResultMatchers.status().isConflict)
        mockMvc.perform(
            MockMvcRequestBuilders.post("${pathTasks}add_task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJsonTask(TITLE_LOR + "2", DESCRIPTION_LOR, "2003-01-12"))
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)

    }

    @Test
    fun updateStateById() {
        val lor = Task(TITLE_LOR, DESCRIPTION_LOR, LocalDate.parse(DEADLINE_LOR))
        eventCalendarRepository.save(lor)
        val newState = TaskState.DOING
        mockMvc.perform(
            MockMvcRequestBuilders.put("${pathTasks}change_state/id/${lor.id!! + 100}?state=DONE")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
        mockMvc.perform(
            MockMvcRequestBuilders.put("${pathTasks}change_state/id/${lor.id}?state=RANDOM")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)

        mockMvc.perform(
            MockMvcRequestBuilders.put("${pathTasks}change_state/id/${lor.id}?state=${lor.state}")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)

        val actual = mockMvc.perform(
            MockMvcRequestBuilders.put("${pathTasks}change_state/id/${lor.id}?state=$newState")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn().response.contentAsString
        val actualObject = mapper.readValue<Task>(actual)
        val expected = eventCalendarRepository.findById(lor.id!!).get()
        assertEquals(actualObject,expected)
        assertEquals(actualObject.state, newState)
    }

    @Test
    fun updateStateByTitle() {
        val lor = Task(TITLE_LOR, DESCRIPTION_LOR, LocalDate.parse(DEADLINE_LOR))
        eventCalendarRepository.save(lor)
        val newState = TaskState.DOING
        mockMvc.perform(
            MockMvcRequestBuilders.put("${pathTasks}change_state/title/random_title?state=DONE")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
        mockMvc.perform(
            MockMvcRequestBuilders.put("${pathTasks}change_state/title/${lor.title}?state=RANDOM")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)

        mockMvc.perform(
            MockMvcRequestBuilders.put("${pathTasks}change_state/title/${lor.title}?state=${lor.state}")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)

        val actual = mockMvc.perform(
            MockMvcRequestBuilders.put("${pathTasks}change_state/title/${lor.title}?state=DOING")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn().response.contentAsString
        assertEquals(eventCalendarRepository.findById(lor.id!!).get().state, newState)
        val actualObject = mapper.readValue<Task>(actual)
        val expected = eventCalendarRepository.findById(lor.id!!).get()
        assertEquals(actualObject,expected)
        assertEquals(actualObject.state, newState)
    }

    @Test
    fun updateDeadlineById() {
        val lor = Task(TITLE_LOR, DESCRIPTION_LOR, LocalDate.parse(DEADLINE_LOR))
        eventCalendarRepository.save(lor)
        val newDeadline = "2023-12-12"
        mockMvc.perform(
            MockMvcRequestBuilders.put("${pathTasks}change_deadline/id/${lor.id!! + 100}?deadline=$newDeadline")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)

        mockMvc.perform(
            MockMvcRequestBuilders.put("${pathTasks}change_deadline/id/${lor.id}?deadline=2022-13-23")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)
        mockMvc.perform(
            MockMvcRequestBuilders.put("${pathTasks}change_deadline/id/${lor.id}?deadline=${lor.deadline}")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)

        val actual = mockMvc.perform(
            MockMvcRequestBuilders.put("${pathTasks}change_deadline/id/${lor.id}?deadline=$newDeadline")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn().response.contentAsString
        val actualObject = mapper.readValue<Task>(actual)
        val expected = eventCalendarRepository.findById(lor.id!!).get()
        assertEquals(actualObject,expected)
        assertEquals(actualObject.deadline, LocalDate.parse(newDeadline))
    }

    @Test
    fun updateDeadlineByTitle() {
        val lor = Task(TITLE_LOR, DESCRIPTION_LOR, LocalDate.parse(DEADLINE_LOR))
        eventCalendarRepository.save(lor)
        val newDeadline = "2023-12-12"
        mockMvc.perform(
            MockMvcRequestBuilders.put("${pathTasks}change_deadline/title/random_title?deadline=$newDeadline")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
        mockMvc.perform(
            MockMvcRequestBuilders.put("${pathTasks}change_deadline/title/${lor.title}?deadline=2022-13-23")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)
        mockMvc.perform(
            MockMvcRequestBuilders.put("${pathTasks}change_deadline/title/${lor.title}?deadline=${lor.deadline}")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)
        val actual = mockMvc.perform(
            MockMvcRequestBuilders.put("${pathTasks}change_deadline/title/${lor.title}?deadline=$newDeadline")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn().response.contentAsString
        val actualObject = mapper.readValue<Task>(actual)
        val expected = eventCalendarRepository.findById(lor.id!!).get()
        assertEquals(actualObject,expected)
        assertEquals(actualObject.deadline, LocalDate.parse(newDeadline))
    }

    @Test
    fun deleteTask() {
        val lor = Task(TITLE_LOR, DESCRIPTION_LOR, LocalDate.parse(DEADLINE_LOR))
        eventCalendarRepository.save(lor)
        mockMvc.perform(
            MockMvcRequestBuilders.delete("${pathTasks}remove_task/${lor.id!! + 1000}")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)

        mockMvc.perform(
            MockMvcRequestBuilders.delete("${pathTasks}remove_task/${(lor.id)}")
        ).andExpect(MockMvcResultMatchers.status().isOk)
        assertFalse(eventCalendarRepository.existsById(lor.id!!))
    }

    @Test
    fun clearDoneTasks() {
        val lor1 = Task(TITLE_LOR, DESCRIPTION_LOR, LocalDate.parse(DEADLINE_LOR))
        eventCalendarRepository.save(lor1)
        val lor2 = Task(TITLE_LOR + "2", DESCRIPTION_LOR, LocalDate.parse("2022-12-29"))
        lor2.state = TaskState.DONE
        eventCalendarRepository.save(lor2)
        val got = Task(TITLE_GOT, DESCRIPTION_GOT, LocalDate.parse(DEADLINE_GOT))
        got.state = TaskState.DONE
        eventCalendarRepository.save(got)
        assertEquals(eventCalendarRepository.count(), 3)
        mockMvc.perform(
            MockMvcRequestBuilders.delete("${pathTasks}clear_done_tasks")
        ).andExpect(MockMvcResultMatchers.status().isOk)
        assertTrue(!eventCalendarRepository.findById(lor2.id!!).isPresent)
        assertTrue(!eventCalendarRepository.findById(got.id!!).isPresent)
    }
    @Test
    fun assignTask(){
        val erfan = User(ERFAN_FIRST_NAME, ERFAN_LAST_NAME, ERFAN_EMAIL)
        val lor = Task(TITLE_LOR, DESCRIPTION_LOR, LocalDate.parse(DEADLINE_LOR))
        userRepository.save(erfan)
        eventCalendarRepository.save(lor)
        mockMvc.perform(
            MockMvcRequestBuilders.put("${pathTasks}assign/${lor.id!! + 1000}?userid=${erfan.id}").contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
        mockMvc.perform(
            MockMvcRequestBuilders.put("${pathTasks}assign/${lor.id}?userid=100").contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
        val actualTasks = mockMvc.perform(
            MockMvcRequestBuilders.put("${pathTasks}assign/${lor.id}?userid=${erfan.id}").contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn().response.contentAsString
        assertEquals(actualTasks,"true")
    }
    @Test
    fun getTasksByUser(){
//        val erfan = User(ERFAN_FIRST_NAME, ERFAN_LAST_NAME, ERFAN_EMAIL)
//        val danial = User(DANIAL_FIRST_NAME, DANIAL_LAST_NAME, DANIAL_EMAIL)
//        val lor = Task(TITLE_LOR, DESCRIPTION_LOR, LocalDate.parse(DEADLINE_LOR))
//        val got = Task(TITLE_GOT, DESCRIPTION_GOT, LocalDate.parse(DEADLINE_GOT))
//        (erfan.assignedTasks as ArrayList).add(lor)
//        (danial.assignedTasks as ArrayList).add(lor)
//        (erfan.assignedTasks as ArrayList).add(got)
//        userRepository.saveAll(listOf(erfan,danial))
//        eventCalendarRepository.saveAll(listOf(lor,got))
//        mockMvc.perform(
//            MockMvcRequestBuilders.get("${pathTasks}get_tasks_by_user/user_id/${erfan.id!! + 1000}")
//        ).andExpect(MockMvcResultMatchers.status().isNotFound)
//
//        val actualUsers = mockMvc.perform(
//            MockMvcRequestBuilders.get("${pathTasks}get_tasks_by_user/user_id/${erfan.id}")
//        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn().response.contentAsString
//
//        val actualList = JSONArray(actualUsers)
//        assertEquals(actualList.length(), 2)
//        val actualLORObject = mapper.readValue<Task>(actualList.getString(0))
//        val actualGOTObject = mapper.readValue<Task>(actualList.getString(1))
//        assertEquals(lor, actualLORObject)
//        assertEquals(got, actualGOTObject)
    }
    final fun toJsonTask(title: String, description: String, deadline: String): String {
        return "{  \"title\": \"$title\",  \"description\": \"$description\",  \"deadline\": \"$deadline\"}"
    }

    final fun toJsonTask(id: Long, title: String, description: String, deadline: String): String {
        return "{ \"id\" : \"$id\",  \"title\": \"$title\",  \"description\": \"$description\",  \"deadline\": \"$deadline\"}"
    }
}