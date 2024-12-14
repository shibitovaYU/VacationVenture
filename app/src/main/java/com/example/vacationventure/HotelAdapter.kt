package com.example.vacationventure

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import android.net.Uri
import android.content.Intent

class HotelAdapter(private val hotels: List<Hotel>) : RecyclerView.Adapter<HotelAdapter.HotelViewHolder>() {

    class HotelViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val hotelImage: ImageView = view.findViewById(R.id.hotel_image)
        val hotelName: TextView = view.findViewById(R.id.hotel_name)
        val hotelPrice: TextView = view.findViewById(R.id.hotel_price)
        val hotelSecondaryInfo: TextView = view.findViewById(R.id.hotel_secondary_info)
        val hotelBubbleRating: TextView = view.findViewById(R.id.hotel_bubble_rating)
        val viewAllDeals: TextView = view.findViewById(R.id.view_all_deals) // Reference for the "view all deals" TextView
    }

    private var translator: Translator? = null

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

        // Function for translating text
        fun translateText(text: String?, callback: (String) -> Unit) {
            if (text.isNullOrEmpty()) {
                callback("") // Return empty string if no text
            } else {
                translator?.translate(text)
                    ?.addOnSuccessListener { translatedText -> callback(translatedText) }
                    ?.addOnFailureListener { callback(text) }
            }
        }

        // Handle hotel title
        val rawTitle = hotel.title
        val titleWithoutNumber = if (rawTitle.length > 3 && rawTitle[1] == '.' && rawTitle[2] == ' ') {
            rawTitle.substring(3) // Remove number from title
        } else if (rawTitle.length > 4 && rawTitle[2] == '.' && rawTitle[3] == ' ') {
            rawTitle.substring(4)
        } else {
            rawTitle
        }

        translateText(titleWithoutNumber) { translatedName ->
            holder.hotelName.text = translatedName
        }

        // Set price in rubles
        val priceInRubles = convertDollarsToRubles(hotel.priceForDisplay)
        setTextIfNotNull(holder.hotelPrice, priceInRubles)


        // Set secondary info
        translateText(hotel.secondaryInfo) { translatedSecondaryInfo ->
            holder.hotelSecondaryInfo.text = translatedSecondaryInfo
        }

        // Set rating
        holder.hotelBubbleRating.text = if (hotel.bubbleRating.rating > 0) {
            "Рейтинг: ${hotel.bubbleRating.rating} (${hotel.bubbleRating.count} отзывов)"
        } else {
            "Рейтинг отсутствует"
        }

        // Load image using Glide
        Glide.with(holder.hotelImage.context)
            .load(hotel.cardPhotos.firstOrNull())
            .placeholder(R.drawable.dialog_fon1) // Placeholder while loading image
            .error(R.drawable.placeholder_image) // Error image if loading fails
            .into(holder.hotelImage)
    }

    override fun getItemCount(): Int = hotels.size

    // Release resources used by the translator
    fun release() {
        translator?.close()
    }

    // Convert price from dollars to rubles
    private fun convertDollarsToRubles(priceInDollars: String?): String {
        val dollarToRubleRate = 100 // 1 dollar = 100 rubles
        return if (!priceInDollars.isNullOrEmpty()) {
            try {
                val price = priceInDollars.replace("$", "").toDouble()
                val priceInRubles = price * dollarToRubleRate
                "₽${String.format("%.2f", priceInRubles)}"
            } catch (e: NumberFormatException) {
                priceInDollars ?: "Цена не указана"
            }
        } else {
            "Цена не указана"
        }
    }

    // Set text if it's not null or empty
    private fun setTextIfNotNull(textView: TextView, text: String?) {
        if (text.isNullOrEmpty()) {
            textView.visibility = View.GONE
        } else {
            textView.visibility = View.VISIBLE
            textView.text = text
        }
    }
}
