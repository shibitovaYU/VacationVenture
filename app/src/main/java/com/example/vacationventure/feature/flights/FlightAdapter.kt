package com.example.vacationventure

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vacationventure.model.FlightSegment

class FlightAdapter(private val flightSegments: List<FlightSegment>) :
    RecyclerView.Adapter<FlightAdapter.FlightViewHolder>() {

    class FlightViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val departureTime: TextView = itemView.findViewById(R.id.departureTime)
        val departureStation: TextView = itemView.findViewById(R.id.departureStation)
        val departureDate: TextView = itemView.findViewById(R.id.departureDate)
        val arrivalTime: TextView = itemView.findViewById(R.id.arrivalTime)
        val arrivalStation: TextView = itemView.findViewById(R.id.arrivalStation)
        val arrivalDate: TextView = itemView.findViewById(R.id.arrivalDate)
        val duration: TextView = itemView.findViewById(R.id.duration)
        val detailLink: TextView = itemView.findViewById(R.id.detail_link)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlightViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_flight, parent, false)
        return FlightViewHolder(view)
    }

    override fun onBindViewHolder(holder: FlightViewHolder, position: Int) {
        val segment = flightSegments[position]
        holder.title.text = segment.thread.title

        val departureHourMinute = segment.departure.substring(11, 16)
        holder.departureTime.text = departureHourMinute
        holder.departureStation.text = "Аэропорт: ${segment.from.title}" // Станция отправления

        val arrivalHourMinute       = segment.arrival.substring(11, 16)
        holder.arrivalTime.text     = arrivalHourMinute
        holder.arrivalStation.text  = "Аэропорт: ${segment.to.title}" // Станция прибытия

        holder.departureDate.text   = segment.start_date
        holder.arrivalDate.text     = segment.arrival.substring(0,10)

        val durationHours = segment.duration / 3600 // Часы
        val durationMinutes = (segment.duration % 3600) / 60 // Минуты
        holder.duration.text = "Длительность: $durationHours ч $durationMinutes мин"

        holder.detailLink.setOnClickListener {
            val url = "https://travel.yandex.ru/avia/flights/${segment.thread.number.replace(" ","-")}/?when=${segment.start_date}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = flightSegments.size
}
