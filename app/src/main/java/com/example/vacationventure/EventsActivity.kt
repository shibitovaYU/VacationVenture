package com.example.vacationventure

import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.vacationventure.models.Event
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class EventsActivity : AppCompatActivity() {

    private lateinit var eventListView: ListView
    private lateinit var eventAdapter: EventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events)

        eventListView = findViewById(R.id.eventListView)

        // Получаем события из Intent
        val eventsJson = intent.getStringExtra("events") ?: return
        val events: List<Event> = Gson().fromJson(eventsJson, object : TypeToken<List<Event>>() {}.type)

        showEvents(events)
    }
    private fun showEvents(events: List<Event>) {
        eventAdapter = EventAdapter(this, events) // Убедитесь, что конструктор корректен
        eventListView.adapter = eventAdapter
    }
}
