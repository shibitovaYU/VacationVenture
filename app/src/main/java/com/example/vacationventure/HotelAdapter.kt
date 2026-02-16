package com.example.vacationventure

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.nl.translate.Translation

class HotelAdapter(private val hotels: MutableList<Hotel>) : RecyclerView.Adapter<HotelAdapter.HotelViewHolder>() {

    class HotelViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val hotelImage: ImageView = view.findViewById(R.id.hotel_image)
        val hotelName: TextView = view.findViewById(R.id.hotel_name)
        val hotelPrice: TextView = view.findViewById(R.id.hotel_price)
        val hotelSecondaryInfo: TextView = view.findViewById(R.id.hotel_secondary_info)
        val hotelBubbleRating: TextView = view.findViewById(R.id.hotel_bubble_rating)
        val eventLink: TextView = view.findViewById(R.id.event_link)
        val favoriteIcon: ImageView = view.findViewById(R.id.hotel_favorite_icon)
    }

    private var translator: Translator? = null
    private val auth = FirebaseAuth.getInstance()
    private val favoritesDb = FirebaseDatabase.getInstance("https://vacationventure-28a86-default-rtdb.europe-west1.firebasedatabase.app/")
        .getReference("favorites_hotels")

    init {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.RUSSIAN)
            .build()
        translator = Translation.getClient(options)
        translator?.downloadModelIfNeeded()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hotel, parent, false)
        return HotelViewHolder(view)
    }

    override fun onBindViewHolder(holder: HotelViewHolder, position: Int) {
        val hotel = hotels[position]

        fun translateText(text: String?, callback: (String) -> Unit) {
            if (text.isNullOrEmpty()) callback("")
            else translator?.translate(text)?.addOnSuccessListener(callback)?.addOnFailureListener { callback(text) }
        }

        translateText(hotel.title) { holder.hotelName.text = it.ifBlank { hotel.title } }
        holder.hotelPrice.text = convertDollarsToRubles(hotel.priceForDisplay)
        translateText(hotel.secondaryInfo) { holder.hotelSecondaryInfo.text = it }

        holder.hotelBubbleRating.text = if (hotel.bubbleRating.rating > 0) {
            "Рейтинг: ${hotel.bubbleRating.rating} (${hotel.bubbleRating.count} отзывов)"
        } else "Рейтинг отсутствует"

        Glide.with(holder.hotelImage.context)
            .load(hotel.cardPhotos.firstOrNull())
            .placeholder(R.drawable.dialog_fon1)
            .error(R.drawable.placeholder_image)
            .into(holder.hotelImage)

        holder.eventLink.text = "Карта / отзывы / фото"
        holder.eventLink.setOnClickListener {
            val query = Uri.encode("${hotel.title} reviews photos map")
            val url = hotel.tripAdvisorUrl ?: "https://www.google.com/search?q=$query"
            holder.itemView.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        checkFavoriteState(hotel, holder.favoriteIcon)
        holder.favoriteIcon.setOnClickListener { toggleFavorite(hotel, holder.favoriteIcon) }

        holder.itemView.setOnClickListener {
            val query = Uri.encode("${hotel.title} map")
            val url = "https://www.google.com/search?q=$query"
            holder.itemView.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    override fun getItemCount(): Int = hotels.size

    fun updateHotels(updated: List<Hotel>) {
        hotels.clear()
        hotels.addAll(updated)
        notifyDataSetChanged()
    }

    fun release() {
        translator?.close()
    }

    private fun toggleFavorite(hotel: Hotel, icon: ImageView) {
        val userId = auth.currentUser?.uid ?: return
        val ref = favoritesDb.child(userId).child(hotel.id)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    ref.removeValue()
                    icon.setImageResource(R.drawable.ic_favorite_border)
                    Toast.makeText(icon.context, "Удалено из избранного", Toast.LENGTH_SHORT).show()
                } else {
                    ref.setValue(hotel)
                    icon.setImageResource(R.drawable.ic_favorite_filled)
                    Toast.makeText(icon.context, "Добавлено в избранное", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) = Unit
        })
    }

    private fun checkFavoriteState(hotel: Hotel, icon: ImageView) {
        val userId = auth.currentUser?.uid ?: run {
            icon.setImageResource(R.drawable.ic_favorite_border)
            return
        }
        favoritesDb.child(userId).child(hotel.id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                icon.setImageResource(if (snapshot.exists()) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border)
            }

            override fun onCancelled(error: DatabaseError) = Unit
        })
    }

    private fun convertDollarsToRubles(priceInDollars: String?): String {
        val dollarToRubleRate = 100
        return if (!priceInDollars.isNullOrEmpty()) {
            try {
                val price = priceInDollars.replace("$", "").toDouble()
                val priceInRubles = price * dollarToRubleRate
                "₽${String.format("%.2f", priceInRubles)}"
            } catch (_: NumberFormatException) {
                priceInDollars
            }
        } else "Цена не указана"
    }
}
