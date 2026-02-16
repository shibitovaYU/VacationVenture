package com.example.vacationventure

import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HotelListActivity : TicketsActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var hotelAdapter: HotelAdapter
    private var allHotels: List<Hotel> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hotel_list)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        allHotels = intent.getParcelableArrayListExtra<Hotel>("hotel_list") ?: emptyList()
        if (allHotels.isNotEmpty()) {
            hotelAdapter = HotelAdapter(allHotels.toMutableList())
            recyclerView.adapter = hotelAdapter
        } else {
            Toast.makeText(this, "Нет данных для отображения", Toast.LENGTH_SHORT).show()
        }

        findViewById<android.widget.TextView>(R.id.filter_hotel_all).setOnClickListener {
            hotelAdapter.updateHotels(allHotels)
        }
        findViewById<android.widget.TextView>(R.id.filter_hotel_high_rating).setOnClickListener {
            hotelAdapter.updateHotels(allHotels.filter { it.bubbleRating.rating >= 4.0 })
        }

        setupNavigationButtons()
        setupBackButton()
    }
}
