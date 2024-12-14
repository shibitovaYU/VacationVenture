package com.example.vacationventure

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vacationventure.MainActivity.TrainSegment
import android.content.Intent
import android.graphics.Color
import android.net.Uri

class TrainAdapter(private val trainSegments: List<TrainSegment>) :
    RecyclerView.Adapter<TrainAdapter.TrainViewHolder>() {

    class TrainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrainViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_train, parent, false)
        return TrainViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrainViewHolder, position: Int) {
        val segment = trainSegments[position]
        holder.title.text = segment.thread.title

        // Форматируем время отправления и прибытия (только ЧЧ:ММ)
        val departureHourMinute = segment.departure.substring(11, 16) // Получаем "HH:mm"
        holder.departureTime.text = departureHourMinute
        holder.departureStation.text = "Станция: ${segment.from.title}" // Станция отправления

        val arrivalHourMinute = segment.arrival.substring(11, 16) // Получаем "HH:mm"
        holder.arrivalTime.text = arrivalHourMinute
        holder.arrivalStation.text = "Станция: ${segment.to.title}" // Станция прибытия

        holder.departureDate.text = segment.start_date // Используйте правильную дату отправления

        // Длительность поездки
        holder.duration.text = "Длительность: ${segment.duration / 3600} ч ${(segment.duration % 3600) / 60} мин"


        holder.detailLink.setOnClickListener {
            val url = "https://rasp.yandex.ru/" // Установите здесь свою ссылку
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            holder.itemView.context.startActivity(intent)
        }
    }
    override fun getItemCount(): Int = trainSegments.size
}
