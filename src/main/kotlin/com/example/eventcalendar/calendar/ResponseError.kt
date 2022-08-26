package com.example.eventcalendar.calendar

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.http.HttpStatus
import java.time.LocalDateTime


class ResponseError(
    status: HttpStatus?,
    message: String?
) {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    val timestamp: LocalDateTime? = LocalDateTime.now()
    var status: Int = status!!.value()
    var error : HttpStatus? = status
    var message: String? = message

    constructor(status: HttpStatus?) : this(status, "No message available") {
    }
}
