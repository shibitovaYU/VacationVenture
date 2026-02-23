package com.example.vacationventure

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


// Базовый класс для всех активностей с кнопками навигации
open class TicketsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    // Функция для настройки кнопок навигации
    protected fun setupNavigationButtons() {
        val favoritesButton: ImageButton = findViewById(R.id.button_favorites)
        val mainButton: ImageButton = findViewById(R.id.button_main)
        val profileButton: ImageButton = findViewById(R.id.button_profile)

        favoritesButton.setOnClickListener {
            val intent = Intent(this, FavoriteActivity::class.java)
            startActivity(intent)
        }

        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        mainButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
    // Функция для настройки кнопки "Назад"
    protected fun setupBackButton() {
        val backButton: TextView = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            finish() // Закрытие текущей активности
        }
    }
}
