package com.example.vacationventure

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDateTime

object RecommendationApiClient {
    private val gson = Gson()
    private val client = OkHttpClient()
    private const val tag = "RecommendationApiClient"
    private val jsonMediaType = "application/json".toMediaType()

    private val baseUrl: String
        get() = BuildConfig.RECOMMENDER_BASE_URL.trimEnd('/')

    data class HomeRecommendationRequest(
        @SerializedName("user_id") val userId: String?,
        val section: String,
        val hour: Int,
        val season: String
    )

    data class HomeRecommendationResponse(
        val message: String
    )

    data class RestaurantCandidate(
        @SerializedName("item_id") val itemId: String,
        val title: String,
        val rating: Double,
        val tags: List<String>,
        @SerializedName("review_count") val reviewCount: Int,
        @SerializedName("price_level") val priceLevel: String?
    )

    data class RestaurantRankingRequest(
        @SerializedName("user_id") val userId: String?,
        val hour: Int,
        val season: String,
        val candidates: List<RestaurantCandidate>
    )

    data class ScoredItem(
        @SerializedName("item_id") val itemId: String,
        val score: Double,
        @SerializedName("is_cold_start") val isColdStart: Boolean
    )

    data class RestaurantRankingResponse(
        @SerializedName("ranked_items") val rankedItems: List<ScoredItem>
    )

    fun fetchHomeSuggestion(userId: String?, section: String, onComplete: (String?) -> Unit) {
        Thread {
            try {
                val now = LocalDateTime.now()
                val payload = HomeRecommendationRequest(
                    userId = userId,
                    section = section.lowercase(),
                    hour = now.hour,
                    season = resolveSeason(now.monthValue)
                )

                val request = Request.Builder()
                    .url("$baseUrl/api/v1/recommendations/home")
                    .post(gson.toJson(payload).toRequestBody(jsonMediaType))
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.w(tag, "Home suggestion failed: ${response.code}")
                        onComplete(null)
                        return@Thread
                    }

                    val body = response.body?.string().orEmpty()
                    val suggestion = gson.fromJson(body, HomeRecommendationResponse::class.java)
                    onComplete(suggestion.message)
                }
            } catch (exception: Exception) {
                Log.w(tag, "Home suggestion error", exception)
                onComplete(null)
            }
        }.start()
    }

    fun rerankRestaurants(
        userId: String?,
        restaurants: List<Restaurant>,
        onComplete: (List<Restaurant>, String?) -> Unit
    ) {
        Thread {
            try {
                val now = LocalDateTime.now()
                val payload = RestaurantRankingRequest(
                    userId = userId,
                    hour = now.hour,
                    season = resolveSeason(now.monthValue),
                    candidates = restaurants.map {
                        RestaurantCandidate(
                            itemId = it.restaurantsId,
                            title = it.name,
                            rating = it.averageRating,
                            tags = listOfNotNull(it.currentOpenStatusText, it.priceTag),
                            reviewCount = it.userReviewCount,
                            priceLevel = it.priceTag
                        )
                    }
                )

                val request = Request.Builder()
                    .url("$baseUrl/api/v1/recommendations/restaurants/rank")
                    .post(gson.toJson(payload).toRequestBody(jsonMediaType))
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.w(tag, "Restaurant ranking failed: ${response.code}")
                        onComplete(restaurants, null)
                        return@Thread
                    }

                    val body = response.body?.string().orEmpty()
                    val ranking = gson.fromJson(body, RestaurantRankingResponse::class.java)
                    val scoreMap = ranking.rankedItems
                        .mapIndexed { index, item -> item.itemId to (ranking.rankedItems.size - index) }
                        .toMap()

                    val sorted = restaurants.sortedByDescending { scoreMap[it.restaurantsId] ?: Int.MIN_VALUE }
                    onComplete(sorted, sorted.firstOrNull()?.restaurantsId)
                }
            } catch (exception: Exception) {
                Log.w(tag, "Restaurant ranking error", exception)
                onComplete(restaurants, null)
            }
        }.start()
    }

    private fun resolveSeason(monthValue: Int): String {
        return when (monthValue) {
            12, 1, 2 -> "winter"
            3, 4, 5 -> "spring"
            6, 7, 8 -> "summer"
            else -> "autumn"
        }
    }
}
