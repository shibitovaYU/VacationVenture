package com.example.vacationventure

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vacationventure.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.content.Intent

class RecommendationSettingsActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    // RadioGroup для каждого вопроса
    private lateinit var question1Group: RadioGroup
    private lateinit var question2Group: RadioGroup
    private lateinit var question3Group: RadioGroup
    private lateinit var question4Group: RadioGroup
    private lateinit var question5Group: RadioGroup

    private lateinit var submitButton: Button
    private lateinit var backButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recommendation_settings)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://vacationventure-28a86-default-rtdb.europe-west1.firebasedatabase.app/").getReference("user_preferences")

        question1Group = findViewById(R.id.question1_group)
        question2Group = findViewById(R.id.question2_group)
        question3Group = findViewById(R.id.question3_group)
        question4Group = findViewById(R.id.question4_group)
        question5Group = findViewById(R.id.question5_group)
        backButton = findViewById(R.id.back_button)

        submitButton = findViewById(R.id.submit_button)
        submitButton.setOnClickListener { saveUserPreferences() }


        backButton.setOnClickListener {
            finish()
        }
    }
    private fun saveUserPreferences() {
        val userId = firebaseAuth.currentUser?.uid ?: return run {
            Toast.makeText(this, "Пожалуйста, войдите в систему", Toast.LENGTH_SHORT).show()
        }

        // Сбор ответов пользователя
        val question1Answer = findViewById<RadioButton>(question1Group.checkedRadioButtonId)?.text.toString()
        val question2Answer = findViewById<RadioButton>(question2Group.checkedRadioButtonId)?.text.toString()
        val question3Answer = findViewById<RadioButton>(question3Group.checkedRadioButtonId)?.text.toString()
        val question4Answer = findViewById<RadioButton>(question4Group.checkedRadioButtonId)?.text.toString()
        val question5Answer = findViewById<RadioButton>(question5Group.checkedRadioButtonId)?.text.toString()

        val userPreferences = UserPreferences(
            question1Answer,
            question2Answer,
            question3Answer,
            question4Answer,
            question5Answer
        )

        // Сохранение в Firebase
        database.child(userId).setValue(userPreferences).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Результаты успешно отправлены", Toast.LENGTH_SHORT).show()

                // Переход на страницу профиля
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                finish() // Закрыть текущую активность
            } else {
                Toast.makeText(this, "Ошибка при отправке результатов: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("message", "Отлично! Мы подумаем, что вам предложить!")
        startActivity(intent)
        finish()
    }
}