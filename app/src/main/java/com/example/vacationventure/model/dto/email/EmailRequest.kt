package com.example.vacationventure.model.dto.email

import com.example.vacationventure.Content
import com.example.vacationventure.Personalization

data class EmailRequest(
    val personalizations: List<Personalization>,
    val from: Email,
    val subject: String,
    val content: List<Content>
)


