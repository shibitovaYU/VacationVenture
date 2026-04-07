package com.example.vacationventure

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

class HotelAdapter(
    private val hotels: List<Hotel>,
    private val onDetailsClick: ((Hotel) -> Unit)? = null,
    private val onTripAdvisorClick: ((Hotel) -> Unit)? = null
) : RecyclerView.Adapter<HotelAdapter.HotelViewHolder>() {

    class HotelViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val hotelImage: ImageView = view.findViewById(R.id.hotel_image)
        val hotelName: TextView = view.findViewById(R.id.hotel_name)
        val hotelPrimaryInfo: TextView = view.findViewById(R.id.hotel_primary_info)
        val hotelSecondaryInfo: TextView = view.findViewById(R.id.hotel_secondary_info)
        val hotelPrice: TextView = view.findViewById(R.id.hotel_price)
        val hotelBubbleRating: TextView = view.findViewById(R.id.hotel_bubble_rating)
        val viewAllDeals: TextView = view.findViewById(R.id.view_all_deals)
        val eventLink: TextView = view.findViewById(R.id.event_link)
    }

    private val translator: Translator by lazy {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.RUSSIAN)
            .build()
        Translation.getClient(options)
    }

    init {
        translator.downloadModelIfNeeded()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hotel, parent, false)
        return HotelViewHolder(view)
    }

    override fun onBindViewHolder(holder: HotelViewHolder, position: Int) {
        val hotel = hotels[position]

        val cleanedTitle = removeLeadingNumber(hotel.title)
        translateText(cleanedTitle) { translated ->
            holder.hotelName.text = translated ?: cleanedTitle
        }

        translateText(hotel.secondaryInfo) { translated ->
            setTextOrHide(holder.hotelSecondaryInfo, translated)
        }

        translateText(hotel.primaryInfo) { translated ->
            setTextOrHide(holder.hotelPrimaryInfo, translated)
        }

        holder.hotelBubbleRating.text = formatRating(
            rating = hotel.bubbleRating.rating,
            reviewsCount = hotel.count
        )

        setTextOrHide(holder.hotelPrice, formatPrice(hotel.priceForDisplay))

        if (!hotel.tripAdvisorUrl.isNullOrBlank()) {
            holder.viewAllDeals.visibility = View.VISIBLE
            holder.viewAllDeals.text = "Открыть на TripAdvisor"
            holder.viewAllDeals.setOnClickListener {
                onTripAdvisorClick?.invoke(hotel)
            }
        } else {
            holder.viewAllDeals.visibility = View.GONE
            holder.viewAllDeals.setOnClickListener(null)
        }

        holder.eventLink.setOnClickListener {
            onDetailsClick?.invoke(hotel)
        }

        val imageUrl = buildImageUrl(hotel.cardPhotos.firstOrNull())
        if (!imageUrl.isNullOrBlank()) {
            holder.hotelImage.visibility = View.VISIBLE
            Glide.with(holder.hotelImage.context)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .centerCrop()
                .into(holder.hotelImage)
        } else {
            holder.hotelImage.visibility = View.VISIBLE
            holder.hotelImage.setImageResource(R.drawable.placeholder_image)
        }
    }

    override fun getItemCount(): Int = hotels.size

    fun release() {
        translator.close()
    }

    private fun translateText(text: String?, callback: (String?) -> Unit) {
        val cleaned = cleanIfNull(text)
        if (cleaned.isNullOrBlank()) {
            callback(null)
            return
        }

        translator.translate(cleaned)
            .addOnSuccessListener { translated ->
                callback(translated)
            }
            .addOnFailureListener {
                callback(cleaned)
            }
    }

    private fun cleanIfNull(text: String?): String? {
        if (text.isNullOrBlank()) return null

        val cleaned = text.trim()
        return if (
            cleaned.equals("null", ignoreCase = true) ||
            cleaned.equals("none", ignoreCase = true) ||
            cleaned.equals("undefined", ignoreCase = true)
        ) {
            null
        } else {
            cleaned
        }
    }

    private fun setTextOrHide(textView: TextView, text: String?) {
        val cleaned = cleanIfNull(text)
        if (cleaned.isNullOrBlank()) {
            textView.visibility = View.GONE
        } else {
            textView.visibility = View.VISIBLE
            textView.text = cleaned
        }
    }

    private fun removeLeadingNumber(title: String): String {
        return title.replace(Regex("""^\d+\.\s*"""), "").trim()
    }

    private fun formatPrice(price: String?): String? {
        val cleaned = cleanIfNull(price) ?: return null
        val lower = cleaned.lowercase()

        val mainPrice = when {
            "view all" in lower -> cleaned.substringBefore(" - ").trim()
            else -> cleaned
        }

        if (mainPrice.isBlank()) return null

        return "Цена: $mainPrice"
    }

    private fun formatRating(rating: Double, reviewsCount: Int): String {
        return if (rating > 0) {
            if (reviewsCount > 0) {
                "Рейтинг: $rating ($reviewsCount отзывов)"
            } else {
                "Рейтинг: $rating"
            }
        } else {
            "Рейтинг отсутствует"
        }
    }

    private fun buildImageUrl(urlTemplate: String?, width: Int = 800, height: Int = 600): String? {
        if (urlTemplate.isNullOrBlank()) return null

        return urlTemplate
            .replace("{width}", width.toString())
            .replace("{height}", height.toString())
    }
}