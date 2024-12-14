package com.example.vacationventure

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Модель вопроса
data class Question(
    val questionText: String,
    val answers: List<String>
)

// Адаптер для отображения вопросов и вариантов ответов
class QuestionAdapter(
    private val context: Context,
    private val questions: List<Question>,
    private val onAnswerSelected: (Int, String) -> Unit
) : RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder>() {

    inner class QuestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val questionText: TextView = itemView.findViewById(R.id.question_text)
        val answersRadioGroup: RadioGroup = itemView.findViewById(R.id.answers_radio_group)

        fun bind(question: Question, position: Int) {
            questionText.text = question.questionText

            // Добавляем варианты ответов
            answersRadioGroup.removeAllViews() // Очищаем предыдущие ответы
            question.answers.forEach { answer ->
                val radioButton = RadioButton(context)
                radioButton.text = answer
                answersRadioGroup.addView(radioButton)

                // Устанавливаем слушатель для выбора ответа
                radioButton.setOnClickListener {
                    onAnswerSelected(position, answer)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_question, parent, false)
        return QuestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(questions[position], position)
    }

    override fun getItemCount(): Int = questions.size
}
