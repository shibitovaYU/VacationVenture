package com.example.vacationventure

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.vacationventure.models.Event
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import android.widget.ImageButton

class EventResultsActivity : TicketsActivity() {

    private lateinit var eventsContainer: LinearLayout
    private lateinit var backButton: TextView
    private lateinit var noEventsView: TextView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_results)

        setupNavigationButtons()
        setupBackButton()

        // Инициализация элементов
        eventsContainer = findViewById(R.id.events_container)
        backButton = findViewById(R.id.back_button)
        noEventsView = findViewById(R.id.no_events_view)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://vacationventure-28a86-default-rtdb.europe-west1.firebasedatabase.app/").getReference("favorites")

        // Получение данных из Intent
        val events: ArrayList<Event> = intent.getParcelableArrayListExtra("events") ?: arrayListOf()
        val selectedDate = intent.getStringExtra("selected_date") ?: ""

        // Проверка на выбор даты
        if (selectedDate.isEmpty()) {
            Toast.makeText(
                this,
                "Пожалуйста, выберите дату и город для поиска мероприятий, без этого мы не сможем найти информацию.",
                Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }

        // Логируем полученные данные
        Log.d("EventResultsActivity", "Selected date: $selectedDate")
        Log.d("EventResultsActivity", "Number of events received: ${events.size}")

        // Форматирование даты
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedDateObj = dateFormat.parse(selectedDate) ?: Date()

        // Фильтрация событий по выбранной дате
        val eventsOnSelectedDate = events.filter {
            val eventDate = dateFormat.parse(it.dates.start.localDate)
            eventDate != null && eventDate == selectedDateObj
        }

        // Отображение событий на выбранную дату
        if (eventsOnSelectedDate.isNotEmpty()) {
            noEventsView.visibility = View.GONE
            eventsOnSelectedDate.forEach { event ->
                addEventToLayout(event)
            }
        } else {
            // Сообщение, если на выбранную дату мероприятий нет
            noEventsView.visibility = View.VISIBLE
            noEventsView.text = "Нет доступных мероприятий."
        }

        // Добавляем блок с предложениями на ближайшие две недели
        addSuggestions(events, selectedDateObj)

        backButton.setOnClickListener {
            finish()
        }
    }
    private fun addSuggestions(events: ArrayList<Event>, selectedDateObj: Date) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // События на ближайшие две недели
        val twoWeeksLater = Calendar.getInstance().apply {
            time = selectedDateObj
            add(Calendar.DAY_OF_YEAR, 14)
        }.time

        val upcomingEvents = events.filter {
            val eventDate = dateFormat.parse(it.dates.start.localDate)
            eventDate != null && eventDate.after(selectedDateObj) && eventDate.before(twoWeeksLater)
        }

        // Если есть предложения, добавляем подзаголовок и события
        if (upcomingEvents.isNotEmpty()) {
            // Заголовок для предложений
            val suggestionTextView = TextView(this).apply {
                text = "Возможно, захотите сходить позже"
                textSize = 20f
                setTextColor(Color.BLACK)
            }

            // Добавляем подзаголовок в контейнер событий
            eventsContainer.addView(suggestionTextView)

            upcomingEvents.forEach { event ->
                addEventToLayout(event)
            }
        } else {
            // Если предложений нет, обновляем текст noEventsView
            noEventsView.text = "Нет мероприятий на выбранную дату и в течение следующих двух недель."
        }
    }
    private fun addEventToLayout(event: Event) {
        val eventView = LayoutInflater.from(this).inflate(R.layout.item_event, eventsContainer, false)

        val eventImage = eventView.findViewById<ImageView>(R.id.event_image)
        val eventName = eventView.findViewById<TextView>(R.id.event_name)
        val eventDate = eventView.findViewById<TextView>(R.id.event_date)
        val eventVenue = eventView.findViewById<TextView>(R.id.event_venue)
        val eventLink = eventView.findViewById<TextView>(R.id.event_link)
        val favoriteIcon = eventView.findViewById<ImageView>(R.id.favorite_icon)

        eventName.text = event.name
        eventDate.text = "Дата: ${event.dates.start.localDate}"
        eventVenue.text = "Место: ${event._embedded.venues[0].name}"

        val eventUrl = event.url
        eventLink.text = "Подробнее"
        eventLink.setTextColor(Color.parseColor("#082567"));
        eventLink.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(eventUrl))
            startActivity(browserIntent)
        }


        val imageUrl = event.images.firstOrNull()?.url
        if (imageUrl != null) {
            Glide.with(this).load(imageUrl).placeholder(R.drawable.dialog_fon1).error(R.drawable.placeholder_image).into(eventImage)
        } else {
            Log.d("EventResultsActivity", "Image URL is null")
        }

        checkIfFavorite(event, favoriteIcon)

        favoriteIcon.setOnClickListener {
            handleFavoriteClick(event, favoriteIcon) // вызов обработчика
        }

        eventView.setOnClickListener {
            val venueQuery = Uri.encode("${event.name} ${event._embedded.venues[0].name} отзывы фото карта")
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$venueQuery")))
        }

        eventsContainer.addView(eventView)
    }
    private fun handleFavoriteClick(event: Event, favoriteIcon: ImageView) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Сначала войдите в систему", Toast.LENGTH_SHORT).show()
            return
        }

        // Используем безопасное имя события как ключ
        val favoriteKey = safeFirebaseKey(event.name)
        val favoriteRef = database.child(userId).child(favoriteKey)

        favoriteRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Удаляем из избранного
                    favoriteRef.removeValue()
                    favoriteIcon.setImageResource(R.drawable.ic_favorite_border) // Обновляем иконку
                    Toast.makeText(this@EventResultsActivity, "${event.name} удалено из избранного", Toast.LENGTH_SHORT).show()
                } else {
                    // Добавляем в избранное
                    favoriteRef.setValue(event).addOnSuccessListener {
                        favoriteIcon.setImageResource(R.drawable.ic_favorite_filled) // Обновляем иконку
                        Toast.makeText(this@EventResultsActivity, "${event.name} добавлено в избранное", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(this@EventResultsActivity, "Ошибка при добавлении в избранное", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@EventResultsActivity, "Ошибка: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun safeFirebaseKey(eventName: String): String {
        return eventName.replace(".", "_")
            .replace("#", "_")
            .replace("$", "_")
            .replace("[", "_")
            .replace("]", "_")
            .replace(" ", "_") // Можно также заменить пробелы
    }
    private fun checkIfFavorite(event: Event, favoriteIcon: ImageView) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            favoriteIcon.setImageResource(R.drawable.ic_favorite_border) // Иконка не в избранном
            return
        }

        val favoriteKey = safeFirebaseKey(event.name)
        val favoritesRef = database.child(userId).child(favoriteKey)

        favoritesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    favoriteIcon.setImageResource(R.drawable.ic_favorite_filled) // Событие в избранном
                } else {
                    favoriteIcon.setImageResource(R.drawable.ic_favorite_border) // Событие не в избранном
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("EventResultsActivity", "Error checking favorite: ${databaseError.message}")
            }
        })
    }
}
