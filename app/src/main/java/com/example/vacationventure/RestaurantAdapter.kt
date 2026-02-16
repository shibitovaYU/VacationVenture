private fun translateText(text: String, onTranslated: (String) -> Unit) {
    val customTranslations = mapOf(
        "Open now" to "Открыт сейчас",
        "Closed now" to "Закрыт сейчас"
    )

    val translatedText = customTranslations[text]

    if (translatedText != null) {
        onTranslated(translatedText)
        return
    }

    translator.translate(text)
        .addOnSuccessListener { translated ->
            onTranslated(translated)
        }
        .addOnFailureListener {
            onTranslated(text)
        }
}
