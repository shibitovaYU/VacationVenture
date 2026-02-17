package com.example.vacationventure

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class RestaurantDetailActivity : AppCompatActivity() {

    private lateinit var restaurant: Restaurant

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_detail)

        restaurant = intent.getParcelableExtra("restaurant_details") ?: return

        val restaurantImage: ImageView = findViewById(R.id.restaurantImage)
        val restaurantName: TextView = findViewById(R.id.restaurantName)
        val restaurantAddress: TextView = findViewById(R.id.restaurantAddress)
        val restaurantRating: TextView = findViewById(R.id.restaurantRating)
        val restaurantMenuLink: TextView = findViewById(R.id.restaurantMenuLink)

        restaurantName.text = restaurant.name
        restaurantAddress.text = restaurant.currentOpenStatusText ?: "Статус не указан"
        restaurantRating.text = if (restaurant.averageRating > 0.0) {
            "Рейтинг: ${restaurant.averageRating} (${restaurant.userReviewCount} отзывов)"
        } else {
            "Рейтинг отсутствует"
        }

        Glide.with(this)
            .load(restaurant.heroImgUrl)
            .placeholder(R.drawable.dialog_fon1)
            .error(R.drawable.placeholder_image)
            .into(restaurantImage)

        restaurantMenuLink.setOnClickListener {
            val url = restaurant.menuUrl ?: "https://www.google.com/search?q=${Uri.encode("${restaurant.name} меню")}" 
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }
}
