package com.example.eventcalendar.calendar

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.json.JSONArray
import org.junit.jupiter.api.AfterEach
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
internal class TaskServiceTest {

    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var eventCalendarRepository: EventCalendarRepository

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    @Autowired
    val mapper = jacksonObjectMapper()

    final val TITLE_LOR: String = "LOR"
    final val DESCRIPTION_LOR: String = "watch $TITLE_LOR"
    final val DEADLINE_LOR: String = "2022-12-09"
    final val JSON_LOR = toJsonTask(TITLE_LOR, DESCRIPTION_LOR, DEADLINE_LOR)
    final val TITLE_GOT: String = "GOT"
    final val DESCRIPTION_GOT: String = "watch $TITLE_GOT"
    final val DEADLINE_GOT: String = "2023-01-13"
    final val JSON_GOT = toJsonTask(TITLE_GOT, DESCRIPTION_GOT, DEADLINE_GOT)

    @BeforeEach
    fun setupMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    @AfterEach
    fun resetDB() {
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
            MockMvcRequestBuilders.get("http://localhost:8080/api/calendar/tasks")
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
            MockMvcRequestBuilders.get("http://localhost:8080/api/calendar/tasks/id/${(lor.id!! + 10)}")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)

        val actual = mockMvc.perform(
            MockMvcRequestBuilders.get("http://localhost:8080/api/calendar/tasks/id/${(lor.id)}")
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn().response.contentAsString
        assertEquals(lor, mapper.readValue<Task>(actual))
    }

    @Test
    fun getTaskByTitle() {
        val lor = Task(TITLE_LOR, DESCRIPTION_LOR, LocalDate.parse(DEADLINE_LOR))
        eventCalendarRepository.save(lor)
        mockMvc.perform(
            MockMvcRequestBuilders.get("http://localhost:8080/api/calendar/tasks/title/random_title")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)

        val actual = mockMvc.perform(
            MockMvcRequestBuilders.get("http://localhost:8080/api/calendar/tasks/title/${(lor.title)}")
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
            MockMvcRequestBuilders.get("http://localhost:8080/api/calendar/tasks/search_title/$TITLE_LOR")
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn().response.contentAsString
        val actualList = JSONArray(actual)
        assertEquals(actualList.length(), 2)
        assertEquals(lor1, mapper.readValue<Task>(actualList.getString(0)))
        assertEquals(lor2, mapper.readValue<Task>(actualList.getString(1)))
    }

    @Test
    fun getTasksByState() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("http://localhost:8080/api/calendar/tasks/state/random_state")
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)

        val lor1 = Task(TITLE_LOR, DESCRIPTION_LOR, LocalDate.parse(DEADLINE_LOR))
        eventCalendarRepository.save(lor1)
        val lor2 = Task(TITLE_LOR + "2", DESCRIPTION_LOR, LocalDate.parse(DEADLINE_LOR))
        lor2.state = TaskState.DOING
        eventCalendarRepository.save(lor2)
        val got = Task(TITLE_GOT, DESCRIPTION_GOT, LocalDate.parse(DEADLINE_GOT))
        eventCalendarRepository.save(got)
        val actual = mockMvc.perform(
            MockMvcRequestBuilders.get("http://localhost:8080/api/calendar/tasks/state/TO_DO")
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn().response.contentAsString
        val actualList = JSONArray(actual)
        assertEquals(actualList.length(), 2)
        assertEquals(lor1, mapper.readValue<Task>(actualList.getString(0)))
        assertEquals(got, mapper.readValue<Task>(actualList.getString(1)))

        val actual2 = mockMvc.perform(
            MockMvcRequestBuilders.get("http://localhost:8080/api/calendar/tasks/state/DOING")
        ).andExpect(MockMvcResultMatchers.status().isOk).andReturn().response.contentAsString
        val actualListDoing = JSONArray(actual2)
        assertEquals(actualListDoing.length(), 1)
        assertEquals(lor2, mapper.readValue<Task>(actualListDoing.getString(0)))
    }

    @Test
    fun getTaskUntilDeadline() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("http://localhost:8080/api/calendar/tasks/deadline/2022-13-29")
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)
        val lor1 = Task(TITLE_LOR, DESCRIPTION_LOR, LocalDate.parse(DEADLINE_LOR))
        eventCalendarRepository.save(lor1)
        val lor2 = Task(TITLE_LOR + "2", DESCRIPTION_LOR, LocalDate.parse("2022-12-29"))
        eventCalendarRepository.save(lor2)
        val got = Task(TITLE_GOT, DESCRIPTION_GOT, LocalDate.parse(DEADLINE_GOT))
        eventCalendarRepository.save(got)
        val actual = mockMvc.perform(
            MockMvcRequestBuilders.get("http://localhost:8080/api/calendar/tasks/deadline/2022-12-29")
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
            MockMvcRequestBuilders.post("http://localhost:8080/api/calendar/add_task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json_lor)
        ).andExpect(MockMvcResultMatchers.status().isCreated).andReturn().response.contentAsString
        val Task: Task = mapper.readValue(actual)
        val expected = eventCalendarRepository.findTaskByTitleEquals(TITLE_LOR).get()
        assertEquals(Task, expected)
        mockMvc.perform(
            MockMvcRequestBuilders.post("http://localhost:8080/api/calendar/add_task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJsonTask(Task.id!!, TITLE_LOR, DESCRIPTION_LOR, DEADLINE_LOR))
        ).andExpect(MockMvcResultMatchers.status().isConflict)
        mockMvc.perform(
            MockMvcRequestBuilders.post("http://localhost:8080/api/calendar/add_task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJsonTask(TITLE_LOR, DESCRIPTION_LOR + "1", DEADLINE_LOR))
        ).andExpect(MockMvcResultMatchers.status().isConflict)
        mockMvc.perform(
            MockMvcRequestBuilders.post("http://localhost:8080/api/calendar/add_task")
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
            MockMvcRequestBuilders.put("http://localhost:8080/api/calendar/change_state/id/${lor.id!! + 100}?state=DONE")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
        mockMvc.perform(
            MockMvcRequestBuilders.put("http://localhost:8080/api/calendar/change_state/id/${lor.id}?state=RANDOM")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)

        mockMvc.perform(
            MockMvcRequestBuilders.put("http://localhost:8080/api/calendar/change_state/id/${lor.id}?state=${lor.state}")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)

        mockMvc.perform(
            MockMvcRequestBuilders.put("http://localhost:8080/api/calendar/change_state/id/${lor.id}?state=$newState")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk)

        assertEquals(eventCalendarRepository.findById(lor.id!!).get().state, newState)
    }

    @Test
    fun updateStateByTitle() {
        val lor = Task(TITLE_LOR, DESCRIPTION_LOR, LocalDate.parse(DEADLINE_LOR))
        eventCalendarRepository.save(lor)
        val newState = TaskState.DOING
        mockMvc.perform(
            MockMvcRequestBuilders.put("http://localhost:8080/api/calendar/change_state/title/random_title?state=DONE")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)
        mockMvc.perform(
            MockMvcRequestBuilders.put("http://localhost:8080/api/calendar/change_state/title/${lor.title}?state=RANDOM")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)

        mockMvc.perform(
            MockMvcRequestBuilders.put("http://localhost:8080/api/calendar/change_state/title/${lor.title}?state=${lor.state}")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)

        mockMvc.perform(
            MockMvcRequestBuilders.put("http://localhost:8080/api/calendar/change_state/title/${lor.title}?state=DOING")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk)
        assertEquals(eventCalendarRepository.findById(lor.id!!).get().state, newState)
    }

    @Test
    fun updateDeadlineById() {
        val lor = Task(TITLE_LOR, DESCRIPTION_LOR, LocalDate.parse(DEADLINE_LOR))
        eventCalendarRepository.save(lor)
        val newDeadline = "2023-12-12"
        mockMvc.perform(
            MockMvcRequestBuilders.put("http://localhost:8080/api/calendar/change_deadline/id/${lor.id!! + 100}?deadline=$newDeadline")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)

        mockMvc.perform(
            MockMvcRequestBuilders.put("http://localhost:8080/api/calendar/change_deadline/id/${lor.id}?deadline=2022-13-23")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)
        mockMvc.perform(
            MockMvcRequestBuilders.put("http://localhost:8080/api/calendar/change_deadline/id/${lor.id}?deadline=${lor.deadline}")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)

        mockMvc.perform(
            MockMvcRequestBuilders.put("http://localhost:8080/api/calendar/change_deadline/id/${lor.id}?deadline=$newDeadline")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk)
        assertEquals(eventCalendarRepository.findById(lor.id!!).get().deadline, LocalDate.parse(newDeadline))
    }

    @Test
    fun updateDeadlineByTitle() {
        val lor = Task(TITLE_LOR, DESCRIPTION_LOR, LocalDate.parse(DEADLINE_LOR))
        eventCalendarRepository.save(lor)
        val newDeadline = "2023-12-12"
        mockMvc.perform(
            MockMvcRequestBuilders.put("http://localhost:8080/api/calendar/change_deadline/title/random_title?deadline=$newDeadline")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound)

        mockMvc.perform(
            MockMvcRequestBuilders.put("http://localhost:8080/api/calendar/change_deadline/title/${lor.title}?deadline=2022-13-23")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)
        mockMvc.perform(
            MockMvcRequestBuilders.put("http://localhost:8080/api/calendar/change_deadline/title/${lor.title}?deadline=${lor.deadline}")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable)
        mockMvc.perform(
            MockMvcRequestBuilders.put("http://localhost:8080/api/calendar/change_deadline/title/${lor.title}?deadline=$newDeadline")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk)
        assertEquals(eventCalendarRepository.findById(lor.id!!).get().deadline, LocalDate.parse(newDeadline))
    }

    @Test
    fun deleteTask() {
        val lor = Task(TITLE_LOR, DESCRIPTION_LOR, LocalDate.parse(DEADLINE_LOR))
        eventCalendarRepository.save(lor)
        mockMvc.perform(
            MockMvcRequestBuilders.delete("http://localhost:8080/api/calendar/remove_task/${lor.id!! + 1000}")
        ).andExpect(MockMvcResultMatchers.status().isNotFound)

        mockMvc.perform(
            MockMvcRequestBuilders.delete("http://localhost:8080/api/calendar/remove_task/${(lor.id)}")
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
            MockMvcRequestBuilders.delete("http://localhost:8080/api/calendar/clear_done_tasks")
        ).andExpect(MockMvcResultMatchers.status().isOk)
        assertTrue(eventCalendarRepository.findById(lor2.id!!).isEmpty)
        assertTrue(eventCalendarRepository.findById(got.id!!).isEmpty)
    }

    final fun toJsonTask(title: String, description: String, deadline: String): String {
        return "{  \"title\": \"$title\",  \"description\": \"$description\",  \"deadline\": \"$deadline\"}"
    }

    final fun toJsonTask(id: Long, title: String, description: String, deadline: String): String {
        return "{ \"id\" : \"$id\",  \"title\": \"$title\",  \"description\": \"$description\",  \"deadline\": \"$deadline\"}"
    }
}