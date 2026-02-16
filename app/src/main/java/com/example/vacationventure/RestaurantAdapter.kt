package com.example.vacationventure

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import com.bumptech.glide.Glide
import android.content.Intent
import android.net.Uri
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.nl.translate.Translation

class RestaurantAdapter(
    private val restaurants: MutableList<Restaurant>,
    private val onClick: (Restaurant) -> Unit
) : RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {

    private var recommendedRestaurantId: String? = null

    class RestaurantViewHolder(itemView: View, private val onClick: (Restaurant) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val restaurantImage: ImageView = itemView.findViewById(R.id.restaurantImage)
        private val restaurantName: TextView = itemView.findViewById(R.id.restaurantName)
        private val restaurantAddress: TextView = itemView.findViewById(R.id.restaurantAddress)
        private val restaurantRating: TextView = itemView.findViewById(R.id.restaurantRating)
        private val restaurantMenuLink: TextView = itemView.findViewById(R.id.restaurantMenuLink)
        private val recommendationBadge: TextView = itemView.findViewById(R.id.recommendationBadge)
        private val translatorOptions = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.RUSSIAN)
            .build()
        private val translator = Translation.getClient(translatorOptions)

        fun bind(restaurant: Restaurant, isRecommended: Boolean) {
            recommendationBadge.visibility = if (isRecommended) View.VISIBLE else View.GONE

            translator.downloadModelIfNeeded()
                .addOnSuccessListener {
                    translateText(restaurant.name) { translatedName ->
                        restaurantName.text = translatedName
                    }
                    translateText(
                        restaurant.currentOpenStatusText ?: "Не указан"
                    ) { translatedAddress ->
                        restaurantAddress.text = translatedAddress
                    }
                }
                .addOnFailureListener {
                    restaurantName.text = restaurant.name
                    restaurantAddress.text = restaurant.currentOpenStatusText ?: "Не указан"
                }

            restaurantRating.text = "Рейтинг: ${restaurant.averageRating}"
            if (!restaurant.heroImgUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(restaurant.heroImgUrl)
                    .into(restaurantImage)
            } else {
                restaurantImage.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            if (!restaurant.menuUrl.isNullOrEmpty()) {
                restaurantMenuLink.text = "Меню ресторана"
                restaurantMenuLink.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(restaurant.menuUrl))
                    itemView.context.startActivity(intent)
                }
            } else {
                restaurantMenuLink.visibility = View.GONE
            }

            itemView.setOnClickListener { onClick(restaurant) }
        }

        private fun translateText(text: String, onTranslated: (String) -> Unit) {
            val customTranslations = mapOf(
                "Open now" to "Открыт сейчас",
                "Closed now" to "Закрыт сейчас"
            )

            val translatedText = customTranslations[text] ?: text

            if (translatedText != text) {
                onTranslated(translatedText)
            } else {
                translator.translate(text)
                    .addOnSuccessListener { translated ->
                        onTranslated(translated)
                    }
                    .addOnFailureListener {
                        onTranslated(text)
                    }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_restaurant, parent, false)
        return RestaurantViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        val restaurant = restaurants[position]
        holder.bind(restaurant, recommendedRestaurantId == restaurant.restaurantsId)
    }

    override fun getItemCount() = restaurants.size

    fun updateRestaurants(updatedRestaurants: List<Restaurant>, recommendedId: String?) {
        restaurants.clear()
        restaurants.addAll(updatedRestaurants)
        recommendedRestaurantId = recommendedId
        notifyDataSetChanged()
    }
}
