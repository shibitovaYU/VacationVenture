package com.example.vacationventure

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HotelListActivity : TicketsActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var hotelAdapter: HotelAdapter
    private var allHotels: List<Hotel> = emptyList()

    private lateinit var detailedToggleButton: TextView
    private lateinit var detailedFiltersRow: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hotel_list)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        detailedToggleButton = findViewById(R.id.filter_hotel_toggle_details)
        detailedFiltersRow = findViewById(R.id.filter_hotel_detailed_row)

        allHotels = intent.getParcelableArrayListExtra<Hotel>("hotel_list") ?: emptyList()
        if (allHotels.isNotEmpty()) {
            hotelAdapter = HotelAdapter(allHotels.toMutableList())
            recyclerView.adapter = hotelAdapter
            setupFilters()
        } else {
            Toast.makeText(this, "Нет данных для отображения", Toast.LENGTH_SHORT).show()
        }

        setupNavigationButtons()
        setupBackButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::hotelAdapter.isInitialized) {
            hotelAdapter.release()
        }
    }

    private fun setupFilters() {
        findViewById<TextView>(R.id.filter_hotel_all).setOnClickListener {
            hotelAdapter.updateHotels(allHotels)
        }
        findViewById<TextView>(R.id.filter_hotel_high_rating).setOnClickListener {
            hotelAdapter.updateHotels(allHotels.filter { it.bubbleRating.rating >= 4.0 })
        }
        findViewById<TextView>(R.id.filter_hotel_budget).setOnClickListener {
            hotelAdapter.updateHotels(allHotels.filter { parsePrice(it.priceForDisplay) <= 10000.0 })
        }
        findViewById<TextView>(R.id.filter_hotel_center).setOnClickListener {
            hotelAdapter.updateHotels(
                allHotels.filter {
                    it.secondaryInfo?.contains("center", true) == true ||
                        it.secondaryInfo?.contains("центр", true) == true
                }
            )
        }
        findViewById<TextView>(R.id.filter_hotel_with_photos).setOnClickListener {
            hotelAdapter.updateHotels(allHotels.filter { it.cardPhotos.isNotEmpty() })
        }

        detailedToggleButton.setOnClickListener {
            val shouldShow = detailedFiltersRow.visibility != View.VISIBLE
            detailedFiltersRow.visibility = if (shouldShow) View.VISIBLE else View.GONE
            detailedToggleButton.text = if (shouldShow) "Подробнее ▲" else "Подробнее ▼"
        }
    }

    private fun parsePrice(priceText: String?): Double {
        if (priceText.isNullOrBlank()) return Double.MAX_VALUE
        return priceText
            .replace("$", "")
            .replace(",", "")
            .trim()
            .toDoubleOrNull()
            ?: Double.MAX_VALUE
    }
}
