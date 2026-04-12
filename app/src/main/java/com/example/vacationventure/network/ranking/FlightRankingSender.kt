package com.example.vacationventure.network.ranking

import android.util.Log
import com.example.vacationventure.model.FlightSegment
import com.example.vacationventure.model.dto.recommendations.FlightRankingRequest
import com.example.vacationventure.model.dto.recommendations.FlightRankingResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FlightRankingSender(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val api: RankingApi = RankingNetwork.api
) {

    suspend fun rankFlights(flights: List<FlightSegment>): FlightRankingResponse? =
        withContext(Dispatchers.IO) {

            val user = auth.currentUser ?: run {
                Log.w("FlightRanking", "No Firebase user")
                return@withContext null
            }

            val token = user.getIdToken(false).await().token ?: run {
                Log.w("FlightRanking", "No Firebase token")
                return@withContext null
            }

            val request = FlightRankingRequest(
                flights = flights
            )

            Log.d("FlightRanking", "Sending ranking request: flights=${flights.size}, userId=${user.uid}")

            val resp = api.rankFlights("Bearer $token", request)

            if (!resp.isSuccessful) {
                Log.e("FlightRanking", "Ranking failed: ${resp.code()} ${resp.message()}")
                return@withContext null
            }

            val body = resp.body()
            Log.d("FlightRanking", "Ranking response: $body")

            body
        }
}