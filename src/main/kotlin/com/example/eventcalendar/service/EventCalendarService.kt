package com.example.eventcalendar.service

import com.example.eventcalendar.repository.EventCalendarRepository
import com.example.eventcalendar.model.TaskState
import com.example.eventcalendar.model.Task
import com.example.eventcalendar.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@Service
class EventCalendarService(calendarRepository: EventCalendarRepository) : IntEventCalendarService {

    private val eventCalendarRepository: EventCalendarRepository = calendarRepository

    @Autowired
    lateinit var userRepository: UserRepository

    override fun getAllTasks(): List<Task> {
        return eventCalendarRepository.findAll()
    }

    override fun getTaskById(id: Long): Task {
        val taskOptional = eventCalendarRepository.findById(id)
        if (!taskOptional.isPresent) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "no task with id $id ")
        }
        return taskOptional.get()
    }

    override fun getTaskByTitle(title: String): Task {
        val taskOptional = eventCalendarRepository.findTaskByTitleEquals(title)
        if (!taskOptional.isPresent) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "no task with title $title")
        }
        return taskOptional.get()
    }

    override fun getTasksBySearchTitle(title: String): List<Task> {
        return eventCalendarRepository.findTasksByTitleContainsIgnoreCase(title).get()
    }

    override fun getTasksByState(state: String): List<Task> {
        try {
            val taskState = TaskState.valueOf(state)
            return eventCalendarRepository.findTaskByState(taskState).get()
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "invalid state!")
        }
    }

    override fun getTasksUntilDeadline(deadline: String): List<Task> {
        try {
            val deadlineLocalDate = if (deadline == "today") LocalDate.now()
            else LocalDate.parse(deadline, DateTimeFormatter.ISO_DATE)
            return eventCalendarRepository.findTasksByDeadlineLessThanEqual(deadlineLocalDate!!).get()
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "invalid date format!")
        }
    }

    override fun addTask(task: Task): Long? {
        if (task.id != null) if (eventCalendarRepository.existsById(task.id!!)) throw ResponseStatusException(
            HttpStatus.CONFLICT, "id is duplicate!"
        )
        if (eventCalendarRepository.existsByTitle(task.title!!)) throw ResponseStatusException(
            HttpStatus.CONFLICT, "title is duplicate!"
        )
        if (task.deadline!!.isBefore(LocalDate.now())) throw ResponseStatusException(
            HttpStatus.NOT_ACCEPTABLE, "deadline is invalid!"
        )
        eventCalendarRepository.save(task)
        return task.id
    }

    @Transactional
    override fun updateStateById(id: Long, state: String): Task {
        val taskOptional = eventCalendarRepository.findById(id)
        if (!taskOptional.isPresent) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "no task with id $id ")
        }
        val task = taskOptional.get()
        val taskState = try {
            TaskState.valueOf(state)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "invalid state!")
        }
        if (task.state == taskState) throw ResponseStatusException(
            HttpStatus.NOT_ACCEPTABLE, "new state and current state are same!"
        )
        task.state = taskState
        return task
    }

    @Transactional
    override fun updateStateByTitle(title: String, state: String): Task {
        val taskOptional = eventCalendarRepository.findTaskByTitleEquals(title)
        if (!taskOptional.isPresent) throw ResponseStatusException(
            HttpStatus.NOT_FOUND, "task with title=$title doesn't exist!"
        )
        val task = taskOptional.get()
        val taskState = try {
            TaskState.valueOf(state)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "invalid state!")
        }
        if (task.state == taskState) throw ResponseStatusException(
            HttpStatus.NOT_ACCEPTABLE, "new state and current state are same!"
        )
        task.state = taskState
        return task
    }

    @Transactional
    override fun updateDeadlineById(id: Long, deadline: String): Task {
        val taskOptional = eventCalendarRepository.findById(id)
        if (!taskOptional.isPresent) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "no task with id $id ")
        }
        val task = taskOptional.get()
        val deadlineLocalDate = try {
            LocalDate.parse(deadline, DateTimeFormatter.ISO_DATE)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "invalid state!")
        }
        if (task.deadline == deadlineLocalDate) throw ResponseStatusException(
            HttpStatus.NOT_ACCEPTABLE, "new deadline and current deadline are same!"
        )
        task.deadline = deadlineLocalDate
        return task
    }

    @Transactional
    override fun updateDeadlineByTitle(title: String, deadline: String): Task {
        val taskOptional = eventCalendarRepository.findTaskByTitleEquals(title)
        if (!taskOptional.isPresent) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "no task with title $title")
        }
        val task = taskOptional.get()
        val deadlineLocalDate = try {
            LocalDate.parse(deadline, DateTimeFormatter.ISO_DATE)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "invalid state!")
        }
        if (task.deadline == deadlineLocalDate) throw ResponseStatusException(
            HttpStatus.NOT_ACCEPTABLE, "new deadline and current deadline are same!"
        )
        task.deadline = deadlineLocalDate
        return task
    }

    @Transactional
    override fun assignTask(taskID: Long, userid: Long?): Boolean {
        val taskOptional = eventCalendarRepository.findById(taskID!!)
        val userOptional = userRepository.findById(userid!!)
        if(!taskOptional.isPresent || !userOptional.isPresent)
            throw ResponseStatusException(HttpStatus.NOT_FOUND,"invalid taskId or userId")
        val task = taskOptional.get()
        val user = userOptional.get()
        if(user.assignedTasks.contains(task))
            throw ResponseStatusException(HttpStatus.CONFLICT,"user has this task")
        user.assignedTasks.add(task)
        return true
    }

    override fun deleteTask(id: Long): Boolean {
        val taskOptional = eventCalendarRepository.findById(id)
        if (!taskOptional.isPresent) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "no task with id $id ")
        }
        eventCalendarRepository.deleteById(id)
        return true
    }

    @Transactional
    override fun clearDoneTasks(): Boolean {
        eventCalendarRepository.deleteTaskByStateEquals(TaskState.DONE)
        return true
    }

    override fun getTasksByUserId(id: Long?): List<Task> {
        val userOptional = userRepository.findById(id!!)
        if (!userOptional.isPresent) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "no user with id $id ")
        }
        return eventCalendarRepository.findTasksByMembersId(id).get()
    }
}
