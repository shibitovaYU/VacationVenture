package com.example.vacationventure

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vacationventure.feature.favorites.FavoriteActivity

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

        // Получение списка ресторанов из Intent
        restaurants = intent.getParcelableArrayListExtra<Restaurant>("restaurant_list") ?: listOf()

        // Настройка RecyclerView
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        restaurantAdapter = RestaurantAdapter(restaurants) { restaurant ->
            // Действие при нажатии на элемент списка, если хотите открыть детальную информацию
        }
        recyclerView.adapter = restaurantAdapter

        favoritesButton.setOnClickListener {
            val intent = Intent(this, FavoriteActivity::class.java)
            startActivity(intent)
        }
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        mainButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        backButton.setOnClickListener {
            finish()
        }
    }
}
