package com.example.vacationventure

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vacationventure.R
import com.example.vacationventure.Restaurant
import android.widget.ImageView
import com.bumptech.glide.Glide
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import com.example.vacationventure.TranslationService
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.nl.translate.Translation

class RestaurantAdapter(
    private val restaurants: List<Restaurant>,
    private val onClick: (Restaurant) -> Unit
) : RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {

    class RestaurantViewHolder(itemView: View, private val onClick: (Restaurant) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val restaurantImage: ImageView = itemView.findViewById(R.id.restaurantImage)
        private val restaurantName: TextView = itemView.findViewById(R.id.restaurantName)
        private val restaurantAddress: TextView = itemView.findViewById(R.id.restaurantAddress)
        private val restaurantRating: TextView = itemView.findViewById(R.id.restaurantRating)
        private val restaurantMenuLink: TextView = itemView.findViewById(R.id.restaurantMenuLink)
        private val translatorOptions = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.RUSSIAN)
            .build()
        private val translator = Translation.getClient(translatorOptions)

        fun bind(restaurant: Restaurant) {
            // Загружаем модель перевода
            translator.downloadModelIfNeeded()
                .addOnSuccessListener {
                    // Переводим название ресторана
                    translateText(restaurant.name) { translatedName ->
                        restaurantName.text = translatedName
                    }
                    // Переводим адрес ресторана
                    translateText(
                        restaurant.currentOpenStatusText ?: "Не указан"
                    ) { translatedAddress ->
                        restaurantAddress.text = translatedAddress
                    }
                }
                .addOnFailureListener {
                    // Если загрузка модели не удалась, оставляем оригинальный текст
                    restaurantName.text = restaurant.name
                    restaurantAddress.text = restaurant.currentOpenStatusText ?: "Не указан"
                }

            restaurantRating.text = "Рейтинг: ${restaurant.averageRating}"
            // Загружаем изображение
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
            // Проверка на специальные фразы и их перевод вручную
            val customTranslations = mapOf(
                "Open now" to "Открыт сейчас",
                "Closed now" to "Закрыт сейчас"
            )

            // Если текст соответствует одной из фраз, используем предопределенный перевод
            val translatedText = customTranslations[text] ?: text

            if (translatedText != text) {
                onTranslated(translatedText)
            } else {
                // Если нет заранее подготовленного перевода, используем ML Kit Translation
                translator.translate(text)
                    .addOnSuccessListener { translated ->
                        onTranslated(translated)
                    }
                    .addOnFailureListener {
                        onTranslated(text) // Возвращаем оригинальный текст при ошибке
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
        holder.bind(restaurants[position])
    }

    override fun getItemCount() = restaurants.size
}

