package com.example.vacationventure

data class EmailRequest(
    val personalizations: List<Personalization>,
    val from: Email,
    val subject: String,
    val content: List<Content>
)


