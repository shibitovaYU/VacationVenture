package com.example.vacationventure

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.vacationventure.SignupActivity
import com.example.vacationventure.databinding.ActivityLogInBinding
import com.google.firebase.auth.FirebaseAuth
import android.text.InputType
import android.view.MotionEvent
import android.net.Uri
import java.security.MessageDigest
import android.util.Base64
import com.example.vacationventure.R

class LogInActivity : AppCompatActivity() {


    private lateinit var codeVerifier: String

    private lateinit var binding: ActivityLogInBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.loginPassword.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.baseline_lock_24, 0, R.drawable.ic_eye_off, 0
        )

        binding.loginPassword.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (binding.loginPassword.right - binding.loginPassword.compoundDrawables[2].bounds.width())) {
                    togglePasswordVisibility()
                    return@setOnTouchListener true
                }
            }
            false
        }

        // Обработка кнопки "Войти"
        binding.loginButton.setOnClickListener {
            val email = binding.loginEmail.text.toString().trim()
            val password = binding.loginPassword.text.toString().trim()

            // Сбрасываем сообщение об ошибке
            binding.errorMessage.text = ""

            // Проверка на пустые поля
            if (email.isEmpty() || password.isEmpty()) {
                binding.errorMessage.text = "Поля не могут быть пустыми"
                return@setOnClickListener
            }

            // Попытка входа с email и паролем
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Успешный вход
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        // Любая ошибка входа - "Неправильный логин или пароль"
                        binding.errorMessage.text = "Неправильный логин или пароль"
                    }
                }
        }


        // Восстановление пароля
        binding.forgotPassword.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.dialog_forgot, null)
            val userEmail = view.findViewById<EditText>(R.id.editBox)

            builder.setView(view)
            val dialog = builder.create()

            view.findViewById<Button>(R.id.btnReset).setOnClickListener {
                compareEmail(userEmail)
                dialog.dismiss()
            }
            view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                dialog.dismiss()
            }
            if (dialog.window != null) {
                dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
            }
            dialog.show()
        }

        binding.signupRedirectText.setOnClickListener {
            val signupIntent = Intent(this, SignupActivity::class.java)
            startActivity(signupIntent)
        }
    }
    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Скрываем пароль
            binding.loginPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.loginPassword.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.baseline_lock_24, 0, R.drawable.ic_eye_off, 0
            )
        } else {
            // Показываем пароль
            binding.loginPassword.inputType = InputType.TYPE_CLASS_TEXT
            binding.loginPassword.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.baseline_lock_24, 0, R.drawable.ic_eye_on, 0
            )
        }

        isPasswordVisible = !isPasswordVisible
        binding.loginPassword.setSelection(binding.loginPassword.text.length)
    }

    // Функции для генерации PKCE-параметров
    private fun generateCodeVerifier(): String {
        val allowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_"
        return (1..64).map { allowedChars.random() }.joinToString("")
    }

    private fun generateCodeChallenge(codeVerifier: String): String {
        val bytes = codeVerifier.toByteArray(Charsets.ISO_8859_1)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return Base64.encodeToString(digest, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    // Функция для проверки email и отправки письма для сброса пароля
    private fun compareEmail(email: EditText) {
        if (email.text.toString().isEmpty()) {
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()) {
            return
        }
        firebaseAuth.sendPasswordResetEmail(email.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Check your email", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
