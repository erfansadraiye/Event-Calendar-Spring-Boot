package com.example.eventcalendar.calendar

import java.time.LocalDate
import javax.persistence.*

@Entity
@Table
class Task(
    id: Long?,
    title: String?,
    description: String?,
    deadline: LocalDate?
) {
    @Id
    @SequenceGenerator(
        name = "task_sequence",
        sequenceName = "task_sequence",
        allocationSize = 1
    )
    @GeneratedValue(
        strategy = GenerationType.AUTO,
        generator = "task_sequence"
    )
    var id: Long? = id

    @Column(unique = true)
    var title: String? = title
    var description: String? = description
    var deadline: LocalDate? = deadline

    @Enumerated(value = EnumType.STRING)
    var state: TaskState? = TaskState.TO_DO

    constructor(title: String, description: String, deadline: LocalDate) :
            this(null, title, description, deadline) {

    }

    constructor() : this(null, null, null, null) {

    }

    override fun equals(other: Any?): Boolean {
        if (other is Task) {
            return id == other.id && description == other.description &&
                    state == other.state && deadline == other.deadline &&
                    title == other.title
        }
        return false
    }
}