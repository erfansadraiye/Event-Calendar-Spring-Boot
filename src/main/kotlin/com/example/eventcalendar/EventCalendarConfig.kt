//package com.example.eventcalendar.calendar
//
//import com.example.eventcalendar.model.User
//import com.example.eventcalendar.repository.UserRepository
//import org.springframework.boot.CommandLineRunner
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import java.time.LocalDate
//import java.time.Month
//
//@Configuration
//class EventCalendarConfig {
//    @Bean
//    fun init(repositoryCalendar: EventCalendarRepository,repositoryUser : UserRepository) = CommandLineRunner{
//        val GOT = Task("watch GOT","watch Game Of Thrones season 1", LocalDate.of(2022, Month.AUGUST,29))
//        val HOD = Task("watch HOD ","watch House Of Dragons season 1", LocalDate.of(2022, Month.DECEMBER,15))
//        val erfan = User("erfan","sadraiye","erfan@gmail.com")
//        val danial = User("danial","jahanbani","danial@gmail.com")
//        repositoryUser.saveAll(listOf(erfan,danial))
//        repositoryCalendar.saveAll(listOf(GOT,HOD))
//    }
//}