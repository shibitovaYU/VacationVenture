package com.example.vacationventure.network.recommendations

import android.util.Log
import com.example.vacationventure.model.dto.recommendations.RecoProfileResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RecoProfileSender(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val api: RecoApi = RecoNetwork.api
) {
    suspend fun getProfile(): RecoProfileResponse? = withContext(Dispatchers.IO) {
        val user = auth.currentUser ?: run {
            Log.w("RecoProfile", "No Firebase user")
            return@withContext null
        }

        val token = user.getIdToken(false).await().token ?: run {
            Log.w("RecoProfile", "No Firebase token")
            return@withContext null
        }

        val resp = api.getRecoProfile("Bearer $token")

        if (!resp.isSuccessful) {
            Log.e("RecoProfile", "Request failed: ${resp.code()} ${resp.message()}")
            return@withContext null
        }

        resp.body()
    }
}