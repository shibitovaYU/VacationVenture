package com.example.vacationventure

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HotelListActivity : TicketsActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var hotelAdapter: HotelAdapter
    private lateinit var favoritesButton: ImageButton
    private lateinit var profileButton: ImageButton
    private lateinit var mainButton: ImageButton
    private lateinit var backButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hotel_list)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Получаем список отелей из Intent
        val hotelList = intent.getParcelableArrayListExtra<Hotel>("hotel_list")
        if (hotelList != null) {
            hotelAdapter = HotelAdapter(hotelList)
            recyclerView.adapter = hotelAdapter
        } else {
            Toast.makeText(this, "Нет данных для отображения", Toast.LENGTH_SHORT).show()
        }
        setupNavigationButtons()
        setupBackButton()
    }
}


