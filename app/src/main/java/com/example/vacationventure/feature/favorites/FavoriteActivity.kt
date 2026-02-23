package com.example.vacationventure

import RecoEventSender
import RecoTracker
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vacationventure.model.FlightSegment
import com.example.vacationventure.model.dto.FlightFavorite.FlightFavoriteData
import com.example.vacationventure.model.dto.recommendations.EventType
import com.example.vacationventure.models.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

class FavoriteActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var favoritesAdapter: FavoritesAdapter
    private var favoritesList = mutableListOf<Pair<String, FlightFavoriteData>>()
    private lateinit var titleTextView: TextView

    private val recoSender = RecoEventSender()
    private lateinit var recoTracker: RecoTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://vacationventure-28a86-default-rtdb.europe-west1.firebasedatabase.app/").getReference("favorites_avia")

        recoTracker = RecoTracker(firebaseAuth, recoSender)

        favoritesRecyclerView = findViewById(R.id.favorites_recycler_view)
        titleTextView = findViewById(R.id.title_text_view)

        favoritesAdapter = FavoritesAdapter(
            favoritesList,
            onRemoveClick = {key, dto -> removeFavoriteByKey(key, dto)}
        )
        favoritesRecyclerView.layoutManager = LinearLayoutManager(this)
        favoritesRecyclerView.adapter = favoritesAdapter

        loadFavorites()

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
                val key = snapshot.key ?: return
                val dto = snapshot.getValue(FlightFavoriteData::class.java) ?: return

                if (favoritesList.none { it.first == key }) {
                    favoritesList.add(key to dto)
                    favoritesAdapter.notifyItemInserted(favoritesList.size - 1)
                    updateTitle()
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val key = snapshot.key ?: return
                val dto = snapshot.getValue(FlightFavoriteData::class.java) ?: return

                val index = favoritesList.indexOfFirst { it.first == key }
                if (index != -1) {
                    favoritesList[index] = key to dto
                    favoritesAdapter.notifyItemChanged(index)
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val key = snapshot.key ?: return
                val index = favoritesList.indexOfFirst { it.first == key }
                if (index != -1) {
                    favoritesList.removeAt(index)
                    favoritesAdapter.notifyItemRemoved(index)
                    updateTitle()
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FavoriteActivity, "Ошибка: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun removeFavoriteByKey(key: String, fav: FlightFavoriteData) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        database.child(userId).child(key).removeValue()
            .addOnSuccessListener {
                lifecycleScope.launch {
                    recoTracker.sendRecoEvent(EventType.UNFAVORITE, fav) // <-- DTO
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Ошибка при удалении", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateTitle() {
        titleTextView.text =
            if (favoritesList.isEmpty()) "В избранном пока ничего нет..."
            else "Вот, что вы сохранили"
    }
}