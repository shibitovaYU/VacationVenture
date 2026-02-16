package com.example.vacationventure

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class RestaurantListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var restaurantAdapter: RestaurantAdapter
    private var restaurants: List<Restaurant> = listOf()
    private lateinit var favoritesButton: ImageButton
    private lateinit var profileButton: ImageButton
    private lateinit var mainButton: ImageButton
    private lateinit var backButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_list)

        favoritesButton = findViewById(R.id.button_favorites)
        mainButton = findViewById(R.id.button_main)
        profileButton = findViewById(R.id.button_profile)
        backButton = findViewById(R.id.back_button)

        restaurants = intent.getParcelableArrayListExtra<Restaurant>("restaurant_list") ?: listOf()

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Открытие Google поиска по клику на ресторан
        restaurantAdapter = RestaurantAdapter(restaurants.toMutableList()) { restaurant ->
            val query = Uri.encode("${restaurant.name} отзывы фото карта")
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$query")))
        }

        recyclerView.adapter = restaurantAdapter

        // Рекомендации (rerank)
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        RecommendationApiClient.rerankRestaurants(currentUserId, restaurants) { ranked, recommendedId ->
            runOnUiThread {
                restaurants = ranked
                restaurantAdapter.updateRestaurants(ranked, recommendedId)
            }
        }

        // Фильтры
        findViewById<TextView>(R.id.filter_restaurant_all).setOnClickListener {
            restaurantAdapter.updateRestaurants(restaurants, null)
        }

        findViewById<TextView>(R.id.filter_restaurant_high_rating).setOnClickListener {
            restaurantAdapter.updateRestaurants(
                restaurants.filter { it.averageRating >= 4.0 },
                null
            )
        }

        findViewById<TextView>(R.id.filter_restaurant_open_now).setOnClickListener {
            restaurantAdapter.updateRestaurants(
                restaurants.filter {
                    it.currentOpenStatusText?.contains("open", true) == true ||
                    it.currentOpenStatusText?.contains("открыт", true) == true
                },
                null
            )
        }

        // Навигация
        favoritesButton.setOnClickListener {
            startActivity(Intent(this, FavoriteActivity::class.java))
        }

        profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        mainButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        backButton.setOnClickListener { finish() }
    }
}
