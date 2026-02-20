package com.example.vacationventure

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditProfileActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var userNameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var backButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Инициализация Firebase Auth и Database
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://vacationventure-28a86-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users")

        userNameEditText = findViewById(R.id.userNameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        saveButton = findViewById(R.id.saveButton)
        backButton = findViewById(R.id.back_button)

        // Обработчик нажатия кнопки "Назад"
        backButton.setOnClickListener {
            finish()
        }

        // Получаем UID текущего пользователя
        val userId = firebaseAuth.currentUser?.uid

        // Загружаем текущее имя пользователя из Firebase Database при старте активности
        if (userId != null) {
            loadUserName(userId)
        }

        // Обработчик нажатия кнопки "Сохранить"
        saveButton.setOnClickListener {
            val newName = userNameEditText.text.toString().trim()
            val newPassword = passwordEditText.text.toString().trim()

            var nameUpdated = false
            var passwordUpdated = false

            // Проверяем, что поле имени не пустое
            if (userId != null && newName.isNotEmpty()) {
                saveUserName(userId, newName) // Сохранение имени
                nameUpdated = true
            } else {
                Toast.makeText(this, "Имя не может быть пустым", Toast.LENGTH_SHORT).show()
            }
            if (newPassword.isNotEmpty()) {
                // Проверка формата пароля
                if (!newPassword.matches(Regex("^[A-Za-z0-9]{6,}$"))) {
                    passwordEditText.error = "Пароль должен содержать только латинские буквы и цифры, длина - минимум 6 символов"
                    return@setOnClickListener
                }
                updatePassword(newPassword) // Обновление пароля
                passwordUpdated = true
            } else {
                passwordUpdated = true // Если пароль не изменён, считаем его обновлённым
            }

            checkCompletion(nameUpdated, passwordUpdated) // Проверяем, завершены ли обновления
        }
    }

    private fun loadUserName(userId: String) {
        database.child(userId).child("name").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val name = dataSnapshot.getValue(String::class.java)
                if (name != null) {
                    userNameEditText.setText(name)
                } else {
                    Toast.makeText(this@EditProfileActivity, "Имя не найдено", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@EditProfileActivity, "Ошибка загрузки данных: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun saveUserName(userId: String, newName: String) {
        database.child(userId).child("name").setValue(newName).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Имя обновлено", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Ошибка при обновлении имени", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun updatePassword(newPassword: String) {
        firebaseAuth.currentUser?.updatePassword(newPassword)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Пароль обновлен", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Ошибка при обновлении пароля: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun checkCompletion(nameUpdated: Boolean, passwordUpdated: Boolean) {
        if (nameUpdated && passwordUpdated) {
            // Возвращаемся на страницу профиля
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish() // Завершаем текущее активити
        }
    }
}
