package com.example.vacationventure

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.view.View
import com.example.vacationventure.MainActivity.FlightSearchResponse
import com.example.vacationventure.MainActivity.FlightSegment

class FlightTicketsActivity : TicketsActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flight_tickets)

        setupNavigationButtons()
        setupBackButton()

        val flightResponse = intent.getParcelableExtra<FlightSearchResponse>("flightResponse")
        val headerText = findViewById<TextView>(R.id.header_text)
        recyclerView = findViewById(R.id.recyclerView)

        if (flightResponse?.segments.isNullOrEmpty()) {
            headerText.text = "Извините, пока тут ничего нет. Попробуйте выбрать другую дату."
            recyclerView.visibility = View.GONE
        } else {
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = FlightAdapter(flightResponse?.segments ?: emptyList())
        }
    }
}
