package com.example.vacationventure

import RecoEventSender
import RecoTracker
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vacationventure.model.FlightSearchResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.vacationventure.model.FlightSegment
import com.example.vacationventure.model.dto.FlightFavorite.FlightFavoriteData
import com.example.vacationventure.model.dto.recommendations.EventType
import com.example.vacationventure.model.dto.recommendations.ItemSnapshot
import com.example.vacationventure.model.dto.recommendations.RecoEvent
import com.example.vacationventure.model.dto.recommendations.SearchContext
import com.google.firebase.database.*
import kotlinx.coroutines.launch


class FlightTicketsActivity : TicketsActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val recoSender = RecoEventSender()
    private lateinit var recoTracker: RecoTracker
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flight_tickets)
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://vacationventure-28a86-default-rtdb.europe-west1.firebasedatabase.app/").getReference("favorites_avia")

        recoTracker = RecoTracker(firebaseAuth, recoSender)

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
            recyclerView.adapter = FlightAdapter(
                flightResponse!!.segments,
                onFavoriteClick = { segment, icon -> handleFavoriteClick(segment, icon) },
                onBindFavoriteState = { segment, icon -> checkIfFavorite(segment, icon) },
                onDetailsClick = {segment ->
                    lifecycleScope.launch {
                        recoTracker.sendRecoEvent(EventType.CLICK, segment)
                    }
                }
            )
        }
    }
    private fun safeFirebaseKey(raw: String): String {
        return raw.replace("[.#$\\[\\]]".toRegex(), "_")
            .replace(" ", "_")
            .replace(":", "_")
            .replace("/", "_")
    }

    private fun flightKey(segment: FlightSegment): String {
        // Делай ключ уникальным: откуда-куда-дата-время-номер
        val raw = "${segment.from.code}_${segment.to.code}_${segment.departure}_${segment.thread.number}"
        return safeFirebaseKey(raw)
    }

    private fun buildFlightDetailUrl(segment: FlightSegment): String {
        val flightId = segment.thread.number.replace(" ", "-")
        return "https://travel.yandex.ru/avia/flights/$flightId/?when=${segment.start_date}"
    }

    private fun handleFavoriteClick(segment: FlightSegment, favoriteIcon: ImageView) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Сначала войдите в систему", Toast.LENGTH_SHORT).show()
            return
        }

        val key = flightKey(segment)
        val ref = database.child(userId).child(key)

        val dto = FlightFavoriteData(
            itemId = "${segment.thread.uid}|${segment.start_date}",
            threadUid = segment.thread.uid,

            title = segment.thread.title,

            fromCode = segment.from.code,
            toCode = segment.to.code,
            fromTitle = segment.from.title,
            toTitle = segment.to.title,

            departure = segment.departure,
            arrival = segment.arrival,
            startDate = segment.start_date,
            duration = segment.duration,
            number = segment.thread.number,

            detailUrl = buildFlightDetailUrl(segment)
        )

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    ref.removeValue()
                        .addOnSuccessListener {
                            favoriteIcon.setImageResource(R.drawable.ic_favorite_border)
                            Toast.makeText(this@FlightTicketsActivity, "Удалено из избранного", Toast.LENGTH_SHORT).show()

                            lifecycleScope.launch {
                                recoTracker.sendRecoEvent(EventType.UNFAVORITE, segment)
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@FlightTicketsActivity, "Ошибка при удалении", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    ref.setValue(dto)
                        .addOnSuccessListener {
                            favoriteIcon.setImageResource(R.drawable.ic_favorite_filled)
                            Toast.makeText(this@FlightTicketsActivity, "Добавлено в избранное", Toast.LENGTH_SHORT).show()

                            lifecycleScope.launch {
                                recoTracker.sendRecoEvent(EventType.FAVORITE, segment)
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@FlightTicketsActivity, "Ошибка при добавлении", Toast.LENGTH_SHORT).show()
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FlightTicketsActivity, "Ошибка: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkIfFavorite(segment: FlightSegment, favoriteIcon: ImageView) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            favoriteIcon.setImageResource(R.drawable.ic_favorite_border)
            return
        }

        val key = flightKey(segment)
        val ref = database.child(userId).child(key)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                favoriteIcon.setImageResource(
                    if (snapshot.exists()) R.drawable.ic_favorite_filled
                    else R.drawable.ic_favorite_border
                )
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FlightTicketsActivity", "Error checking favorite: ${error.message}")
            }
        })
    }

}
