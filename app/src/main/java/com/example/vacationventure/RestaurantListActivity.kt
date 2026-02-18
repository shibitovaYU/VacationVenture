package com.example.vacationventure

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
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
    private var recommendedRestaurantId: String? = null

    private lateinit var favoritesButton: ImageButton
    private lateinit var profileButton: ImageButton
    private lateinit var mainButton: ImageButton
    private lateinit var backButton: TextView
    private lateinit var detailedToggleButton: TextView
    private lateinit var detailedFiltersRow: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_list)

        favoritesButton = findViewById(R.id.button_favorites)
        mainButton = findViewById(R.id.button_main)
        profileButton = findViewById(R.id.button_profile)
        backButton = findViewById(R.id.back_button)
        detailedToggleButton = findViewById(R.id.filter_restaurant_toggle_details)
        detailedFiltersRow = findViewById(R.id.filter_restaurant_detailed_row)

        restaurants = intent.getParcelableArrayListExtra<Restaurant>("restaurant_list") ?: listOf()

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        restaurantAdapter = RestaurantAdapter(restaurants.toMutableList()) { restaurant ->
            val query = Uri.encode("${restaurant.name} отзывы фото карта")
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$query")))
        }

        recyclerView.adapter = restaurantAdapter

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        RecommendationApiClient.rerankRestaurants(currentUserId, restaurants) { ranked, recommendedId ->
            runOnUiThread {
                restaurants = ranked
                recommendedRestaurantId = recommendedId
                restaurantAdapter.updateRestaurants(ranked, recommendedId)
            }
        }

        setupFilters()

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

    override fun onDestroy() {
        super.onDestroy()
        restaurantAdapter.release()
    }

    private fun setupFilters() {
        findViewById<TextView>(R.id.filter_restaurant_all).setOnClickListener {
            restaurantAdapter.updateRestaurants(restaurants, recommendedRestaurantId)
        }

        findViewById<TextView>(R.id.filter_restaurant_high_rating).setOnClickListener {
            restaurantAdapter.updateRestaurants(
                restaurants.filter { it.averageRating >= 4.0 },
                recommendedRestaurantId
            )
        }

        findViewById<TextView>(R.id.filter_restaurant_open_now).setOnClickListener {
            restaurantAdapter.updateRestaurants(
                restaurants.filter {
                    it.currentOpenStatusText?.contains("open", true) == true ||
                        it.currentOpenStatusText?.contains("открыт", true) == true
                },
                recommendedRestaurantId
            )
        }

        findViewById<TextView>(R.id.filter_restaurant_price_low).setOnClickListener {
            restaurantAdapter.updateRestaurants(
                restaurants.filter { (it.priceTag?.length ?: 0) in 1..2 },
                recommendedRestaurantId
            )
        }

        findViewById<TextView>(R.id.filter_restaurant_with_menu).setOnClickListener {
            restaurantAdapter.updateRestaurants(
                restaurants.filter { !it.menuUrl.isNullOrBlank() },
                recommendedRestaurantId
            )
        }

        findViewById<TextView>(R.id.filter_restaurant_popular).setOnClickListener {
            restaurantAdapter.updateRestaurants(
                restaurants.filter { it.userReviewCount >= 100 },
                recommendedRestaurantId
            )
        }

        detailedToggleButton.setOnClickListener {
            val shouldShow = detailedFiltersRow.visibility != View.VISIBLE
            detailedFiltersRow.visibility = if (shouldShow) View.VISIBLE else View.GONE
            detailedToggleButton.text = if (shouldShow) "Подробнее ▲" else "Подробнее ▼"
        }
    }
}
