package com.example.vacationventure

import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

abstract class BaseSegmentAdapter<T>(
    protected val segments: List<T>
) : RecyclerView.Adapter<BaseSegmentAdapter.BaseViewHolder>() {

    open class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val departureTime: TextView = itemView.findViewById(R.id.departureTime)
        val departureStation: TextView = itemView.findViewById(R.id.departureStation)
        val departureDate: TextView = itemView.findViewById(R.id.departureDate)
        val arrivalTime: TextView = itemView.findViewById(R.id.arrivalTime)
        val arrivalStation: TextView = itemView.findViewById(R.id.arrivalStation)
        val duration: TextView = itemView.findViewById(R.id.duration)
        val detailLink: TextView = itemView.findViewById(R.id.detail_link)
    }

    // Форматирование даты
    protected fun formatDate(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()) // Исходный формат
        val outputFormat = SimpleDateFormat("dd-MM", Locale.getDefault()) // Формат, в который нужно преобразовать

        return try {
            val date = inputFormat.parse(dateString)
            date?.let {
                outputFormat.format(it) // Преобразуем в нужный формат
            } ?: "N/A" // Если дата невалидна, возвращаем "N/A"
        } catch (e: Exception) {
            "N/A" // Если ошибка при парсинге, возвращаем "N/A"
        }
    }

    // Функция для вычисления времени прибытия
    protected fun getArrivalTime(departureDate: String, duration: Int): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd-MM HH:mm", Locale.getDefault())

        return try {
            val departure = inputFormat.parse(departureDate)
            val calendar = Calendar.getInstance()
            calendar.time = departure
            calendar.add(Calendar.SECOND, duration) // Добавляем продолжительность в секундах

            outputFormat.format(calendar.time) // Возвращаем время прибытия в формате "dd-MM HH:mm"
        } catch (e: Exception) {
            "N/A"
        }
    }

    // Общая логика для перехода по ссылке
    protected fun setDetailLinkClickListener(holder: BaseViewHolder) {
        holder.detailLink.setOnClickListener {
            val url = "https://rasp.yandex.ru/" // Можно будет передать динамически URL
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            holder.itemView.context.startActivity(intent)
        }
    }
}
