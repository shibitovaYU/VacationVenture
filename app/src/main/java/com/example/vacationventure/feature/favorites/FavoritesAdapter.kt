package com.example.vacationventure

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.vacationventure.model.FlightSegment
import com.example.vacationventure.model.dto.FlightFavorite.FlightFavoriteData
import com.example.vacationventure.models.Event

class FavoritesAdapter(
    private val favoritesList: MutableList<Pair<String, FlightFavoriteData>>,
    private val onRemoveClick: (String, FlightFavoriteData) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_flight, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val (key, dto) = favoritesList[position]

        holder.title.text = dto.title


        // ISO "2026-02-24T00:20:00+03:00" -> "00:20"
        holder.departureTime.text = dto.departure.takeIf { it.length >= 16 }?.substring(11, 16) ?: dto.departure
        holder.arrivalTime.text = dto.arrival.takeIf { it.length >= 16 }?.substring(11, 16) ?: dto.arrival

        holder.departureStation.text = "Аэропорт: ${dto.fromTitle}"
        holder.arrivalStation.text = "Аэропорт: ${dto.toTitle}"

        holder.departureDate.text = dto.startDate
        holder.arrivalDate.text = dto.arrival.takeIf { it.length >= 10 }?.substring(0, 10) ?: dto.arrival

        val hours = dto.duration / 3600
        val minutes = (dto.duration % 3600) / 60
        holder.duration.text = "Длительность: $hours ч $minutes мин"

        holder.favoriteIcon.setImageResource(R.drawable.ic_favorite_filled)
        holder.favoriteIcon.setOnClickListener { onRemoveClick(key, dto) }

        holder.detailLink.setOnClickListener {
            val url = "https://travel.yandex.ru/avia/flights/${dto.number.replace(" ", "-")}/?when=${dto.startDate}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = favoritesList.size

    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val departureTime: TextView = itemView.findViewById(R.id.departureTime)
        val departureStation: TextView = itemView.findViewById(R.id.departureStation)
        val departureDate: TextView = itemView.findViewById(R.id.departureDate)
        val arrivalTime: TextView = itemView.findViewById(R.id.arrivalTime)
        val arrivalStation: TextView = itemView.findViewById(R.id.arrivalStation)
        val arrivalDate: TextView = itemView.findViewById(R.id.arrivalDate)
        val duration: TextView = itemView.findViewById(R.id.duration)
        val detailLink: TextView = itemView.findViewById(R.id.detail_link)
        val favoriteIcon: ImageView = itemView.findViewById(R.id.favorite_icon)
    }
}
