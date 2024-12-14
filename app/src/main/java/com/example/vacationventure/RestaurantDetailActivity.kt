package com.example.vacationventure

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.vacationventure.Restaurant
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageButton
import com.example.vacationventure.RestaurantListActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class RestaurantDetailActivity : TicketsActivity() {

    private lateinit var restaurant: Restaurant
    private lateinit var restaurantImage: ImageView
    private lateinit var restaurantName: TextView
    private lateinit var restaurantAddress: TextView
    private lateinit var restaurantRating: TextView
    private lateinit var restaurantMenuLink: TextView
    private lateinit var favoritesButton: ImageView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.item_restaurant)

        setupNavigationButtons()
        setupBackButton()

        // Инициализация Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://vacationventure-28a86-default-rtdb.europe-west1.firebasedatabase.app/").getReference("favorites_restaurants")

        // Получение данных о ресторане из Intent
        restaurant = intent.getParcelableExtra<Restaurant>("restaurant_details") ?: return

        // Инфлейт макета item_restaurant и инициализация элементов
        val inflater = LayoutInflater.from(this)
        val iventView = inflater.inflate(R.layout.item_restaurant, null)

        restaurantImage = iventView.findViewById(R.id.restaurantImage)
        restaurantName = iventView.findViewById(R.id.restaurantName)
        restaurantAddress = iventView.findViewById(R.id.restaurantAddress)
        restaurantRating = iventView.findViewById(R.id.restaurantRating)
        restaurantMenuLink = iventView.findViewById(R.id.restaurantMenuLink)

        // Установка данных ресторана
        restaurantName.text = restaurant.name
        restaurantAddress.text = restaurant.currentOpenStatusText
        restaurantRating.text = "Рейтинг: ${restaurant.averageRating}"

        Glide.with(this).load(restaurant.heroImgUrl).into(restaurantImage)
    }
}
