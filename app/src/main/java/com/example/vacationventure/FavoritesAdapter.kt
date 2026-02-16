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
import com.example.vacationventure.model.FavoriteItem

class FavoritesAdapter(
    private val favoritesList: MutableList<FavoriteItem>,
    private val onFavoriteClick: (FavoriteItem) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val item = favoritesList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = favoritesList.size

    fun submitList(updatedItems: List<FavoriteItem>) {
        favoritesList.clear()
        favoritesList.addAll(updatedItems)
        notifyDataSetChanged()
    }

    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val eventImage: ImageView = itemView.findViewById(R.id.event_image)
        private val eventName: TextView = itemView.findViewById(R.id.event_name)
        private val eventDate: TextView = itemView.findViewById(R.id.event_date)
        private val eventVenue: TextView = itemView.findViewById(R.id.event_venue)
        private val eventLink: TextView = itemView.findViewById(R.id.event_link)
        private val favoriteIcon: ImageView = itemView.findViewById(R.id.favorite_icon)

        fun bind(item: FavoriteItem) {
            eventName.text = item.title
            eventDate.text = item.subtitle
            eventVenue.text = item.details

            Glide.with(itemView.context)
                .load(item.imageUrl)
                .placeholder(R.drawable.dialog_fon1)
                .error(R.drawable.placeholder_image)
                .into(eventImage)

            favoriteIcon.setImageResource(R.drawable.ic_favorite_filled)

            eventLink.text = "Подробнее"
            eventLink.setTextColor(Color.BLUE)
            eventLink.setOnClickListener {
                openExternalUrl(item.externalUrl)
            }

            favoriteIcon.setOnClickListener {
                onFavoriteClick(item)
            }

            itemView.setOnClickListener {
                openExternalUrl(item.externalUrl)
            }
        }

        private fun openExternalUrl(url: String?) {
            if (url.isNullOrBlank()) return
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            itemView.context.startActivity(browserIntent)
        }
    }
}
