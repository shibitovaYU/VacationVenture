package com.example.vacationventure

data class UserPreferences(
    val question1: String = "",
    val question2: String = "",
    val question3: String = "",
    val question4: String = "",
    val question5: String = "",
    var articleIndex: Int = 0 // Значение по умолчанию для индекса статьи
) {
    // Явный конструктор без аргументов
    constructor() : this(
        question1 = "",
        question2 = "",
        question3 = "",
        question4 = "",
        question5 = "",
        articleIndex = 0
    )
}
