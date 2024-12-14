package com.example.vacationventure

import android.os.Bundle
import android.widget.Button
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.widget.TextView
import android.widget.ImageButton
import com.google.firebase.database.*
import android.util.Log
import android.widget.Toast

class ProfileActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var userEmailTextView: TextView
    private lateinit var userNameTextView: TextView
    private lateinit var editProfileButton: Button
    private lateinit var recommendationButton: Button
    private lateinit var supportButton: Button
    private lateinit var logoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Инициализация Firebase Auth и Database
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://vacationventure-28a86-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users")

        // Инициализация элементов интерфейса
        userNameTextView = findViewById(R.id.userNameTextView)
        userEmailTextView = findViewById(R.id.userEmailTextView)
        editProfileButton = findViewById(R.id.editProfileButton)
        recommendationButton = findViewById(R.id.recommendationButton)
        logoutButton = findViewById(R.id.logoutButton)

        val favoritesButton: ImageButton = findViewById(R.id.button_favorites)
        val mainButton: ImageButton = findViewById(R.id.button_main)
        val profileButton: ImageButton = findViewById(R.id.button_profile)

        favoritesButton.setOnClickListener {
            val intent = Intent(this, FavoriteActivity::class.java)
            startActivity(intent)
        }
        mainButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val message = intent.getStringExtra("message")
        message?.let {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        }

        // Получаем UID текущего пользователя
        val userId = firebaseAuth.currentUser?.uid

        // Загружаем данные пользователя
        if (userId != null) {
            loadUserData(userId)
        } else {
            Toast.makeText(this, "Пользователь не найден", Toast.LENGTH_SHORT).show()
        }
    }
    private fun loadUserData(userId: String) {
        Log.d("ProfileActivity", "Загрузка данных для пользователя: $userId")
        database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val name = dataSnapshot.child("name").getValue(String::class.java)
                    val email = firebaseAuth.currentUser?.email // Получаем email текущего пользователя

                    // Установка значений в TextView
                    userNameTextView.text = name ?: "Имя не найдено"
                    userEmailTextView.text = email ?: "Email не найден"
                } else {
                    Log.d("ProfileActivity", "Пользователь не найден в базе данных")
                    userNameTextView.text = "Имя не найдено"
                    userEmailTextView.text = "Email не найден"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("ProfileActivity", "Ошибка загрузки данных: ${databaseError.message}")
                Toast.makeText(this@ProfileActivity, "Ошибка загрузки данных: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // Установка слушателей для кнопок
        setupButtonListeners()
    }
    private fun setupButtonListeners() {
        // Кнопка редактировать профиль
        editProfileButton.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // Кнопка Настройка рекомендаций
        recommendationButton.setOnClickListener {
            startActivity(Intent(this, RecommendationSettingsActivity::class.java))
        }

        // Кнопка выхода
        logoutButton.setOnClickListener {
            firebaseAuth.signOut()
            val intent = Intent(this, LogInActivity::class.java) // Возвращаемся на экран авторизации
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish() // Завершаем текущее Activity
        }
    }
}
