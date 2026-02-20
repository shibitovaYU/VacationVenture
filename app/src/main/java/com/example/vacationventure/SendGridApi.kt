package com.example.vacationventure

import com.example.vacationventure.model.dto.email.EmailRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SendGridApi {
    @POST("mail/send")
    suspend fun sendEmail(@Body emailRequest: EmailRequest): Response<Unit>
}
