package com.example.vacationventure

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vacationventure.feature.events.Event
import com.example.vacationventure.feature.events.EventData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FavoriteActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var favoritesAdapter: FavoritesAdapter
    private var favoritesList = mutableListOf<Event>()
    private lateinit var titleTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://vacationventure-28a86-default-rtdb.europe-west1.firebasedatabase.app/").getReference("favorites")

        favoritesRecyclerView = findViewById(R.id.favorites_recycler_view)
        titleTextView = findViewById(R.id.title_text_view)

        favoritesAdapter = FavoritesAdapter(favoritesList) { event: Event -> toggleFavorite(event) }
        favoritesRecyclerView.layoutManager = LinearLayoutManager(this)
        favoritesRecyclerView.adapter = favoritesAdapter

        loadFavorites()

        val favoritesButton: ImageButton = findViewById(R.id.button_favorites)
        val mainButton: ImageButton = findViewById(R.id.button_main)
        val profileButton: ImageButton = findViewById(R.id.button_profile)

        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        mainButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
    private fun loadFavorites() {
        val userId = firebaseAuth.currentUser?.uid ?: return run {
            Toast.makeText(this, "Сначала войдите в систему", Toast.LENGTH_SHORT).show()
        }

        val favoritesRef = database.child(userId)
        favoritesRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val firebaseEventData = snapshot.getValue(EventData::class.java)
                if (firebaseEventData != null) {
                    val event = Event(
                        name = firebaseEventData.name,
                        dates = firebaseEventData.dates,
                        _embedded = firebaseEventData._embedded,
                        images = firebaseEventData.images,
                        url = firebaseEventData.url
                    )
                    if (!favoritesList.any { it.name == event.name }) {
                        favoritesList.add(event)
                        favoritesAdapter.notifyItemInserted(favoritesList.size - 1)
                        updateTitle()
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val firebaseEventData = snapshot.getValue(EventData::class.java)
                if (firebaseEventData != null) {
                    val eventIndex = favoritesList.indexOfFirst { it.name == firebaseEventData.name }
                    if (eventIndex != -1) {
                        favoritesList[eventIndex] = Event(
                            name = firebaseEventData.name,
                            dates = firebaseEventData.dates,
                            _embedded = firebaseEventData._embedded,
                            images = firebaseEventData.images,
                            url = firebaseEventData.url
                        )
                        favoritesAdapter.notifyItemChanged(eventIndex)
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val firebaseEventData = snapshot.getValue(EventData::class.java)
                if (firebaseEventData != null) {
                    val eventIndex = favoritesList.indexOfFirst { it.name == firebaseEventData.name }
                    if (eventIndex != -1) {
                        favoritesList.removeAt(eventIndex)
                        favoritesAdapter.notifyItemRemoved(eventIndex)
                        updateTitle()
                    }
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@FavoriteActivity, "Ошибка: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun updateTitle() {
        titleTextView.text = if (favoritesList.isEmpty()) {
            "В избранном пока ничего нет..."
        } else {
            "Вот, что вы сохранили"
        }
    }
    private fun toggleFavorite(event: Event) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val favoriteKey = safeFirebaseKey(event.name) // Используем ID события для уникальности
        val favoriteRef = database.child(userId).child(favoriteKey)

        favoriteRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Если элемент уже в избранном, удаляем его из Firebase
                    favoriteRef.removeValue().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Удаляем элемент из списка
                            val eventIndex = favoritesList.indexOfFirst { it.name == event.name }
                            if (eventIndex != -1) {
                                favoritesList.removeAt(eventIndex)
                                favoritesAdapter.notifyItemRemoved(eventIndex)
                                Toast.makeText(this@FavoriteActivity, "${event.name} удалено из избранного", Toast.LENGTH_SHORT).show()
                                updateTitle()
                            }
                        } else {
                            Log.e("FavoriteActivity", "Ошибка удаления: ${task.exception?.message}")
                        }
                    }
                } else {
                    // Если элемента нет в избранном, добавляем его в Firebase
                    favoriteRef.setValue(event).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Проверяем, добавлен ли элемент в список
                            if (!favoritesList.any { it.name == event.name }) {
                                favoritesList.add(event)
                                favoritesAdapter.notifyItemInserted(favoritesList.size - 1)
                                Toast.makeText(this@FavoriteActivity, "${event.name} добавлено в избранное", Toast.LENGTH_SHORT).show()
                                updateTitle()
                            }
                        } else {
                            Log.e("FavoriteActivity", "Ошибка добавления: ${task.exception?.message}")
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FavoriteActivity", "Ошибка чтения: ${databaseError.message}")
            }
        })
    }
    private fun safeFirebaseKey(name: String): String {
        return name.replace("[#$.\\[\\]]".toRegex(), "_").replace(" ", "_") // Замените пробелы
    }
}