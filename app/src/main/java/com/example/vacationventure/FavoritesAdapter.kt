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
import com.example.vacationventure.models.Event

class FavoritesAdapter(
    private val favoritesList: MutableList<Event>,
    private val onFavoriteClick: (Event) -> Unit // Лямбда для обработки кликов по иконке
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val event = favoritesList[position]
        holder.bind(event)
    }

    override fun getItemCount(): Int = favoritesList.size

    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val eventImage: ImageView = itemView.findViewById(R.id.event_image)
        private val eventName: TextView = itemView.findViewById(R.id.event_name)
        private val eventDate: TextView = itemView.findViewById(R.id.event_date)
        private val eventVenue: TextView = itemView.findViewById(R.id.event_venue)
        private val eventLink: TextView = itemView.findViewById(R.id.event_link)
        private val favoriteIcon: ImageView = itemView.findViewById(R.id.favorite_icon)

        fun bind(event: Event) {
            eventName.text = event.name
            eventDate.text = "Дата: ${event.dates.start.localDate}"
            eventVenue.text = "Место: ${event._embedded.venues[0].name}"

            Glide.with(itemView.context).load(event.images.firstOrNull()?.url).into(eventImage)

            favoriteIcon.setImageResource(R.drawable.ic_favorite_filled)

            val eventUrl = event.url
            eventLink.text = "Подробнее"
            eventLink.setTextColor(Color.BLUE)
            eventLink.setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(eventUrl))
                itemView.context.startActivity(browserIntent)  // Используем itemView.context для вызова startActivity
            }

            favoriteIcon.setOnClickListener {
                onFavoriteClick(event)
            }

            itemView.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.url))
                itemView.context.startActivity(intent)
            }
        }
    }
}
