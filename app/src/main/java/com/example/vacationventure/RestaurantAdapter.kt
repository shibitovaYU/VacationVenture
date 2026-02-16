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
        private val favoriteIcon: ImageView = itemView.findViewById(R.id.restaurantFavoriteIcon)

        private val auth = FirebaseAuth.getInstance()
        private val favoritesDb = FirebaseDatabase.getInstance("https://vacationventure-28a86-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("favorites_restaurants")

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
                    translateText(restaurant.currentOpenStatusText ?: "Не указан") { translatedAddress ->
                        restaurantAddress.text = translatedAddress
                    }
                }
                .addOnFailureListener {
                    restaurantName.text = restaurant.name
                    restaurantAddress.text = restaurant.currentOpenStatusText ?: "Не указан"
                }

            restaurantRating.text = "Рейтинг: ${restaurant.averageRating}"
            Glide.with(itemView.context)
                .load(restaurant.heroImgUrl)
                .placeholder(R.drawable.dialog_fon1)
                .error(R.drawable.placeholder_image)
                .into(restaurantImage)

            if (!restaurant.menuUrl.isNullOrEmpty()) {
                restaurantMenuLink.visibility = View.VISIBLE
                restaurantMenuLink.text = "Меню / отзывы"
                restaurantMenuLink.setOnClickListener {
                    itemView.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(restaurant.menuUrl)))
                }
            } else {
                restaurantMenuLink.visibility = View.VISIBLE
                restaurantMenuLink.text = "Карта и отзывы"
                restaurantMenuLink.setOnClickListener {
                    val query = Uri.encode("${restaurant.name} reviews map")
                    val url = "https://www.google.com/search?q=$query"
                    itemView.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            }

            checkFavoriteState(restaurant)
            favoriteIcon.setOnClickListener { toggleFavorite(restaurant) }

            itemView.setOnClickListener {
                onClick(restaurant)
                val query = Uri.encode("${restaurant.name} ${restaurant.currentOpenStatusText ?: ""} map")
                val url = "https://www.google.com/search?q=$query"
                itemView.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }

        private fun toggleFavorite(restaurant: Restaurant) {
            val userId = auth.currentUser?.uid ?: return
            val favoriteRef = favoritesDb.child(userId).child(restaurant.restaurantsId)
            favoriteRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        favoriteRef.removeValue()
                        favoriteIcon.setImageResource(R.drawable.ic_favorite_border)
                        Toast.makeText(itemView.context, "Удалено из избранного", Toast.LENGTH_SHORT).show()
                    } else {
                        favoriteRef.setValue(restaurant)
                        favoriteIcon.setImageResource(R.drawable.ic_favorite_filled)
                        Toast.makeText(itemView.context, "Добавлено в избранное", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) = Unit
            })
        }

        private fun checkFavoriteState(restaurant: Restaurant) {
            val userId = auth.currentUser?.uid ?: run {
                favoriteIcon.setImageResource(R.drawable.ic_favorite_border)
                return
            }
            favoritesDb.child(userId).child(restaurant.restaurantsId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        favoriteIcon.setImageResource(
                            if (snapshot.exists()) R.drawable.ic_favorite_filled
                            else R.drawable.ic_favorite_border
                        )
                    }

                    override fun onCancelled(error: DatabaseError) = Unit
                })
        }

        private fun translateText(text: String, onTranslated: (String) -> Unit) {
            val customTranslations = mapOf(
                "Open now" to "Открыт сейчас",
                "Closed now" to "Закрыт сейчас"
            )

            val translatedText = customTranslations[text] ?: text
            if (translatedText != text) {
                onTranslated(translatedText)
                return
            }

            translator.translate(text)
                .addOnSuccessListener { translated -> onTranslated(translated) }
                .addOnFailureListener { onTranslated(text) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_restaurant, parent, false)
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
