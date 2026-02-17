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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

class RestaurantAdapter(
    private val restaurants: MutableList<Restaurant>,
    private val onItemClick: (Restaurant) -> Unit
) : RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {

    private var recommendedRestaurantId: String? = null
    private val auth = FirebaseAuth.getInstance()
    private val favoritesDb = FirebaseDatabase
        .getInstance("https://vacationventure-28a86-default-rtdb.europe-west1.firebasedatabase.app/")
        .getReference("favorites_restaurants")

    private var translator: Translator? = null

    init {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.RUSSIAN)
            .build()
        translator = Translation.getClient(options)
        translator?.downloadModelIfNeeded()
    }

    class RestaurantViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recommendationBadge: TextView = view.findViewById(R.id.recommendationBadge)
        val image: ImageView = view.findViewById(R.id.restaurantImage)
        val name: TextView = view.findViewById(R.id.restaurantName)
        val address: TextView = view.findViewById(R.id.restaurantAddress)
        val rating: TextView = view.findViewById(R.id.restaurantRating)
        val menuLink: TextView = view.findViewById(R.id.restaurantMenuLink)
        val favoriteIcon: ImageView = view.findViewById(R.id.restaurantFavoriteIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_restaurant, parent, false)
        return RestaurantViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        val restaurant = restaurants[position]

        holder.name.text = restaurant.name
        holder.rating.text = if (restaurant.averageRating > 0.0) {
            "Рейтинг: ${restaurant.averageRating} (${restaurant.userReviewCount} отзывов)"
        } else {
            "Рейтинг отсутствует"
        }

        translateText(restaurant.currentOpenStatusText.orEmpty()) { translated ->
            holder.address.text = translated.ifBlank { "Статус не указан" }
        }

        holder.menuLink.setOnClickListener {
            val url = restaurant.menuUrl ?: run {
                val query = Uri.encode("${restaurant.name} меню")
                "https://www.google.com/search?q=$query"
            }
            holder.itemView.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        Glide.with(holder.image.context)
            .load(restaurant.heroImgUrl)
            .placeholder(R.drawable.dialog_fon1)
            .error(R.drawable.placeholder_image)
            .into(holder.image)

        holder.itemView.setOnClickListener { onItemClick(restaurant) }

        holder.recommendationBadge.visibility =
            if (restaurant.restaurantsId == recommendedRestaurantId) View.VISIBLE else View.GONE

        checkFavoriteState(restaurant, holder.favoriteIcon)
        holder.favoriteIcon.setOnClickListener { toggleFavorite(restaurant, holder.favoriteIcon) }
    }

    override fun getItemCount(): Int = restaurants.size

    fun updateRestaurants(updated: List<Restaurant>, recommendedId: String?) {
        restaurants.clear()
        restaurants.addAll(updated)
        recommendedRestaurantId = recommendedId
        notifyDataSetChanged()
    }

    fun release() {
        translator?.close()
    }

    private fun translateText(text: String, onTranslated: (String) -> Unit) {
        if (text.isBlank()) {
            onTranslated("")
            return
        }

        val customTranslations = mapOf(
            "Open now" to "Открыт сейчас",
            "Closed now" to "Закрыт сейчас"
        )

        val translatedText = customTranslations[text]

        if (translatedText != null) {
            onTranslated(translatedText)
            return
        }

        translator?.translate(text)
            ?.addOnSuccessListener { translated ->
                onTranslated(translated)
            }
            ?.addOnFailureListener {
                onTranslated(text)
            }
            ?: onTranslated(text)
    }

    private fun toggleFavorite(restaurant: Restaurant, icon: ImageView) {
        val userId = auth.currentUser?.uid ?: return
        val ref = favoritesDb.child(userId).child(restaurant.restaurantsId)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    ref.removeValue()
                    icon.setImageResource(R.drawable.ic_favorite_border)
                    Toast.makeText(icon.context, "Удалено из избранного", Toast.LENGTH_SHORT).show()
                } else {
                    ref.setValue(restaurant)
                    icon.setImageResource(R.drawable.ic_favorite_filled)
                    Toast.makeText(icon.context, "Добавлено в избранное", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) = Unit
        })
    }

    private fun checkFavoriteState(restaurant: Restaurant, icon: ImageView) {
        val userId = auth.currentUser?.uid ?: run {
            icon.setImageResource(R.drawable.ic_favorite_border)
            return
        }

        favoritesDb.child(userId).child(restaurant.restaurantsId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    icon.setImageResource(
                        if (snapshot.exists()) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
                    )
                }

                override fun onCancelled(error: DatabaseError) = Unit
            })
    }
}
