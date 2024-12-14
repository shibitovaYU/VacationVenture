package com.example.vacationventure

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.vacationventure.LogInActivity
import com.example.vacationventure.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.signupButton.setOnClickListener {
            val email = binding.signupEmail.text.toString().trim()
            val password = binding.signupPassword.text.toString().trim()
            val confirmPassword = binding.signupConfirm.text.toString().trim()

            // Сбрасываем сообщение об ошибке
            binding.errorMessage.text = ""

            // Проверка на пустые поля
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                binding.errorMessage.text = "Поля не могут быть пустыми"
                return@setOnClickListener
            }

            // Проверка корректности email
            if (!email.matches(Regex("^[A-Za-z0-9._%+-]+@(mail\\.ru|gmail\\.com|yandex\\.ru|outlook\\.com)$"))) {
                binding.errorMessage.text =
                    "Некорректная почта"
                return@setOnClickListener
            }

            // Проверка корректности пароля
            if (!password.matches(Regex("^[A-Za-z0-9]{6,}$"))) {
                binding.errorMessage.text =
                    "Пароль должен содержать только латинские буквы и цифры, длина - не менее 6 символов"
                return@setOnClickListener
            }

            // Проверка совпадения паролей
            if (password != confirmPassword) {
                binding.errorMessage.text = "Пароли должны совпадать"
                return@setOnClickListener
            }

            // Создание пользователя в Firebase
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Успешная регистрация
                        val intent = Intent(this, LogInActivity::class.java)
                        startActivity(intent)
                    } else {
                        // Ошибка регистрации
                        binding.errorMessage.text =
                            "Ошибка регистрации: ${task.exception?.message ?: "Неизвестная ошибка"}"
                    }
                }
        }

    }

}