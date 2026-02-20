package com.example.vacationventure

import android.app.DatePickerDialog
import android.content.Context
import android.widget.DatePicker
import java.util.Calendar

class CustomDatePickerDialog(
    context: Context,
    private val onDateSetCallback: (year: Int, month: Int, day: Int) -> Unit
) : DatePickerDialog(context, R.style.CustomDatePicker, { _: DatePicker, year: Int, month: Int, day: Int ->
    onDateSetCallback(year, month, day)
}, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) {

    init {
        // Настройка окна для кастомного фона или цветов (если нужно)
        window?.setBackgroundDrawableResource(android.R.color.transparent) // Прозрачный фон диалога
    }
}

