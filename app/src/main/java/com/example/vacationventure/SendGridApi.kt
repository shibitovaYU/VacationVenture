package com.example.vacationventure

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response
import com.example.vacationventure.EmailRequest
import okhttp3.ResponseBody

interface SendGridApi {
    @POST("mail/send")
    suspend fun sendEmail(@Body emailRequest: EmailRequest): Response<Unit>
}
