package com.example.vacationventure

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.vacationventure.models.Event

class EventAdapter(
    context: Context,
    private val events: List<Event>
) : ArrayAdapter<Event>(context, 0, events) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.event_item, parent, false)

        val event = events[position]

        // Ищем элементы интерфейса
        val eventNameTextView: TextView = view.findViewById(R.id.event_name)
        val eventDateTextView: TextView = view.findViewById(R.id.event_date)
        val eventVenueTextView: TextView = view.findViewById(R.id.event_venue)

        // Устанавливаем текст
        eventNameTextView.text = event.name
        eventDateTextView.text = event.dates.start.localDate // Предполагается, что это поле доступно
        eventVenueTextView.text = event._embedded.venues[0].name // Предполагается, что это поле доступно

        return view
    }
}
