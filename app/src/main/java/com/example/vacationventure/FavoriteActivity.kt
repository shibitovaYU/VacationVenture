package com.example.vacationventure

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vacationventure.model.FavoriteItem
import com.example.vacationventure.model.FavoriteType
import com.example.vacationventure.models.EventData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FavoriteActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var eventsDb: DatabaseReference
    private lateinit var restaurantsDb: DatabaseReference
    private lateinit var hotelsDb: DatabaseReference
    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var favoritesAdapter: FavoritesAdapter
    private lateinit var titleTextView: TextView
    private lateinit var detailedToggleButton: TextView
    private lateinit var detailedFiltersRow: View

    private val allItems = mutableListOf<FavoriteItem>()
    private var currentFilter = FavoriteType.ALL
    private var detailsFilter = DetailsFilter.ALL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)

        firebaseAuth = FirebaseAuth.getInstance()
        val firebase = FirebaseDatabase.getInstance("https://vacationventure-28a86-default-rtdb.europe-west1.firebasedatabase.app/")
        eventsDb = firebase.getReference("favorites")
        restaurantsDb = firebase.getReference("favorites_restaurants")
        hotelsDb = firebase.getReference("favorites_hotels")

        favoritesRecyclerView = findViewById(R.id.favorites_recycler_view)
        titleTextView = findViewById(R.id.title_text_view)
        detailedToggleButton = findViewById(R.id.filter_favorites_toggle_details)
        detailedFiltersRow = findViewById(R.id.favorites_filters_detailed)

        favoritesAdapter = FavoritesAdapter(mutableListOf()) { item -> toggleFavorite(item) }
        favoritesRecyclerView.layoutManager = LinearLayoutManager(this)
        favoritesRecyclerView.adapter = favoritesAdapter

        initFilterButtons()
        loadFavorites()

        val mainButton: ImageButton = findViewById(R.id.button_main)
        val profileButton: ImageButton = findViewById(R.id.button_profile)

        profileButton.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        mainButton.setOnClickListener { startActivity(Intent(this, MainActivity::class.java)) }
    }

    private fun initFilterButtons() {
        findViewById<TextView>(R.id.filter_all).setOnClickListener {
            currentFilter = FavoriteType.ALL
            applyFilter()
        }
        findViewById<TextView>(R.id.filter_restaurants).setOnClickListener {
            currentFilter = FavoriteType.RESTAURANT
            applyFilter()
        }
        findViewById<TextView>(R.id.filter_events).setOnClickListener {
            currentFilter = FavoriteType.EVENT
            applyFilter()
        }
        findViewById<TextView>(R.id.filter_hotels).setOnClickListener {
            currentFilter = FavoriteType.HOTEL
            applyFilter()
        }

        findViewById<TextView>(R.id.filter_with_images).setOnClickListener {
            detailsFilter = DetailsFilter.WITH_IMAGE
            applyFilter()
        }
        findViewById<TextView>(R.id.filter_with_links).setOnClickListener {
            detailsFilter = DetailsFilter.WITH_LINK
            applyFilter()
        }

        detailedToggleButton.setOnClickListener {
            val shouldShow = detailedFiltersRow.visibility != View.VISIBLE
            detailedFiltersRow.visibility = if (shouldShow) View.VISIBLE else View.GONE
            detailedToggleButton.text = if (shouldShow) "Подробнее ▲" else "Подробнее ▼"
            if (!shouldShow) {
                detailsFilter = DetailsFilter.ALL
                applyFilter()
            }
        }
    }

    private fun loadFavorites() {
        val userId = firebaseAuth.currentUser?.uid ?: return run {
            Toast.makeText(this, "Сначала войдите в систему", Toast.LENGTH_SHORT).show()
        }

        loadEvents(userId)
        loadRestaurants(userId)
        loadHotels(userId)
    }

    private fun loadEvents(userId: String) {
        eventsDb.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allItems.removeAll { it.type == FavoriteType.EVENT }
                snapshot.children.forEach { child ->
                    val event = child.getValue(EventData::class.java) ?: return@forEach
                    val venueName = event._embedded.venues.firstOrNull()?.name ?: "Место не указано"
                    allItems.add(
                        FavoriteItem(
                            id = child.key ?: event.name,
                            type = FavoriteType.EVENT,
                            title = event.name,
                            subtitle = "Дата: ${event.dates.start.localDate}",
                            details = "Место: $venueName",
                            imageUrl = event.images.firstOrNull()?.url,
                            externalUrl = event.url
                        )
                    )
                }
                applyFilter()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FavoriteActivity, "Ошибка: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadRestaurants(userId: String) {
        restaurantsDb.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allItems.removeAll { it.type == FavoriteType.RESTAURANT }
                snapshot.children.forEach { child ->
                    val id = child.child("restaurantsId").getValue(String::class.java)
                        ?.takeIf { it.isNotBlank() }
                        ?: child.key
                        ?: return@forEach
                    val name = child.child("name").getValue(String::class.java) ?: "Ресторан"
                    val rating = child.child("averageRating").getValue(Double::class.java) ?: 0.0
                    val status = child.child("currentOpenStatusText").getValue(String::class.java)
                    val image = child.child("heroImgUrl").getValue(String::class.java)
                    val menuUrl = child.child("menuUrl").getValue(String::class.java)
                    allItems.add(
                        FavoriteItem(
                            id = id,
                            type = FavoriteType.RESTAURANT,
                            title = name,
                            subtitle = "Рейтинг: $rating",
                            details = status ?: "Статус не указан",
                            imageUrl = image,
                            externalUrl = menuUrl
                        )
                    )
                }
                applyFilter()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FavoriteActivity, "Ошибка: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadHotels(userId: String) {
        hotelsDb.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allItems.removeAll { it.type == FavoriteType.HOTEL }
                snapshot.children.forEach { child ->
                    val id = child.child("id").getValue(String::class.java) ?: child.key ?: return@forEach
                    val title = child.child("title").getValue(String::class.java) ?: "Отель"
                    val price = child.child("priceForDisplay").getValue(String::class.java)
                    val secondary = child.child("secondaryInfo").getValue(String::class.java)
                    val externalUrl = child.child("tripAdvisorUrl").getValue(String::class.java)
                    val photos = child.child("cardPhotos").children.firstOrNull()?.getValue(String::class.java)
                    allItems.add(
                        FavoriteItem(
                            id = id,
                            type = FavoriteType.HOTEL,
                            title = title,
                            subtitle = price ?: "Цена не указана",
                            details = secondary ?: "Нет доп. информации",
                            imageUrl = photos,
                            externalUrl = externalUrl
                        )
                    )
                }
                applyFilter()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FavoriteActivity, "Ошибка: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun applyFilter() {
        val byType = when (currentFilter) {
            FavoriteType.ALL -> allItems.toList()
            else -> allItems.filter { it.type == currentFilter }
        }

        val filtered = when (detailsFilter) {
            DetailsFilter.ALL -> byType
            DetailsFilter.WITH_IMAGE -> byType.filter { !it.imageUrl.isNullOrBlank() }
            DetailsFilter.WITH_LINK -> byType.filter { !it.externalUrl.isNullOrBlank() }
        }

        favoritesAdapter.submitList(filtered.sortedBy { it.title })
        updateTitle(filtered.isEmpty())
    }

    private fun updateTitle(isEmpty: Boolean) {
        titleTextView.text = if (isEmpty) {
            "В избранном пока ничего нет..."
        } else {
            "Вот, что вы сохранили"
        }
    }

    private fun toggleFavorite(item: FavoriteItem) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val db = when (item.type) {
            FavoriteType.EVENT -> eventsDb
            FavoriteType.RESTAURANT -> restaurantsDb
            FavoriteType.HOTEL -> hotelsDb
            FavoriteType.ALL -> return
        }

        db.child(userId).child(item.id).removeValue()
        Toast.makeText(this, "${item.title} удалено из избранного", Toast.LENGTH_SHORT).show()
    }

    private enum class DetailsFilter {
        ALL,
        WITH_IMAGE,
        WITH_LINK
    }
}
