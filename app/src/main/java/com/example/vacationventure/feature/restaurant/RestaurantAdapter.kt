package com.example.vacationventure

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

class RestaurantAdapter(
    private val restaurants: List<Restaurant>,
    private val onClick: (Restaurant) -> Unit
) : RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {

    // --- ML Kit Translator (EN -> RU)
    private val translator: Translator by lazy {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.RUSSIAN)
            .build()
        Translation.getClient(options)
    }

    // Кэш переводов (чтобы не переводить одно и то же при скролле)
    private val nameCache = mutableMapOf<Long, String>()
    private val reviewCache = mutableMapOf<Long, String>() // только RU итог

    init {
        // модель скачиваем один раз
        translator.downloadModelIfNeeded()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        translator.close()
    }

    class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.restaurant_image)
        val name: TextView = itemView.findViewById(R.id.restaurant_name)
        val price: TextView = itemView.findViewById(R.id.restaurant_price)
        val cuisine: TextView = itemView.findViewById(R.id.restaurant_cuisine)
        val status: TextView = itemView.findViewById(R.id.restaurant_status)
        val rating: TextView = itemView.findViewById(R.id.restaurant_rating)

        val reviewBox: View = itemView.findViewById(R.id.review_box)
        val reviewText: TextView = itemView.findViewById(R.id.restaurant_review)

        val menuLink: TextView = itemView.findViewById(R.id.menu_link)
        val detailsLink: TextView = itemView.findViewById(R.id.event_link)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_restaurant, parent, false)
        return RestaurantViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        val r = restaurants[position]

        // токен, чтобы async-переводы не приезжали в переиспользованный holder
        holder.itemView.tag = r.locationId

        // --- Image
        if (!r.heroImgUrl.isNullOrBlank()) {
            Glide.with(holder.itemView.context)
                .load(r.heroImgUrl)
                .centerCrop()
                .into(holder.image)
        } else {
            holder.image.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // --- Static texts
        holder.rating.text = buildRating(r.averageRating, r.userReviewCount)
        holder.price.text = buildAvgCheckRub(r.priceTag)
        holder.cuisine.text = buildCuisineRu(r.cuisines)
        holder.status.text = "Статус: ${normalizeStatusRu(r.currentOpenStatusCategory, r.currentOpenStatusText)}"

        // --- Name (translate)
        holder.name.text = r.name
        val cachedName = nameCache[r.locationId]
        if (cachedName != null) {
            holder.name.text = cachedName
        } else {
            translateToRuIfNeeded(
                text = r.name,
                holder = holder,
                onOk = { ru ->
                    nameCache[r.locationId] = ru
                    holder.name.text = ru
                },
                onFail = {
                    // если не удалось — оставляем оригинал
                    holder.name.text = r.name
                }
            )
        }

        // --- Review (RU only)
        holder.reviewBox.visibility = View.GONE
        holder.reviewText.text = ""

        val cachedReview = reviewCache[r.locationId]
        if (cachedReview != null) {
            holder.reviewText.text = "“$cachedReview”"
            holder.reviewBox.visibility = View.VISIBLE
        } else {
            val snippet = r.reviewSnippet?.trim().orEmpty()
            if (snippet.isNotBlank()) {
                if (isRussianText(snippet)) {
                    reviewCache[r.locationId] = snippet
                    holder.reviewText.text = "“$snippet”"
                    holder.reviewBox.visibility = View.VISIBLE
                } else {
                    // переводим и показываем ТОЛЬКО если результат русский
                    translator.translate(snippet)
                        .addOnSuccessListener { ru ->
                            if (holder.itemView.tag != r.locationId) return@addOnSuccessListener
                            if (isRussianText(ru)) {
                                val clean = ru.trim()
                                reviewCache[r.locationId] = clean
                                holder.reviewText.text = "“$clean”"
                                holder.reviewBox.visibility = View.VISIBLE
                            } else {
                                holder.reviewBox.visibility = View.GONE
                            }
                        }
                        .addOnFailureListener {
                            if (holder.itemView.tag != r.locationId) return@addOnFailureListener
                            // строго RU-only → просто скрываем
                            holder.reviewBox.visibility = View.GONE
                        }
                }
            }
        }

        // --- Menu link
        val menuUrl = r.menuUrl
        if (!menuUrl.isNullOrBlank()) {
            holder.menuLink.visibility = View.VISIBLE
            holder.menuLink.setOnClickListener {
                holder.itemView.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(menuUrl)))
            }
        } else {
            holder.menuLink.visibility = View.GONE
            holder.menuLink.setOnClickListener(null)
        }

        // --- Details link ("Подробнее") -> reviewUrl
        val detailsUrl = r.reviewUrl
        if (!detailsUrl.isNullOrBlank()) {
            holder.detailsLink.visibility = View.VISIBLE
            holder.detailsLink.setOnClickListener {
                holder.itemView.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(detailsUrl)))
            }

            // по твоему пожеланию: можно открывать "Подробнее" кликом по названию
            holder.name.setOnClickListener {
                holder.itemView.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(detailsUrl)))
            }
        } else {
            holder.detailsLink.visibility = View.GONE
            holder.detailsLink.setOnClickListener(null)
            holder.name.setOnClickListener(null)
        }

        holder.itemView.setOnClickListener { onClick(r) }
    }

    override fun getItemCount() = restaurants.size

    // ---------------- helpers ----------------

    private fun translateToRuIfNeeded(
        text: String,
        holder: RestaurantViewHolder,
        onOk: (String) -> Unit,
        onFail: (() -> Unit)? = null
    ) {
        if (text.isBlank() || isRussianText(text)) {
            onOk(text)
            return
        }

        val bindId = holder.itemView.tag

        translator.translate(text)
            .addOnSuccessListener { translated ->
                if (holder.itemView.tag != bindId) return@addOnSuccessListener
                onOk(translated)
            }
            .addOnFailureListener {
                if (holder.itemView.tag != bindId) return@addOnFailureListener
                onFail?.invoke()
            }
    }

    private fun isRussianText(s: String): Boolean =
        s.any { it in 'А'..'я' || it == 'ё' || it == 'Ё' }

    private fun buildRating(avg: Double?, count: Int?): String {
        val a = if (avg != null && avg > 0) String.format(Locale.US, "%.1f", avg) else "—"
        val c = if (count != null && count > 0) count.toString() else "0"
        return "Рейтинг: $a ($c отзывов)"
    }

    private fun buildCuisineRu(tags: List<String>): String {
        if (tags.isEmpty()) return "Кухня: —"
        val localized = tags.take(4).map { t ->
            when (t.trim().lowercase()) {
                "indian" -> "Индийская"
                "asian" -> "Азиатская"
                "chinese" -> "Китайская"
                "japanese" -> "Японская"
                "italian" -> "Итальянская"
                "lebanese" -> "Ливанская"
                "mediterranean" -> "Средиземноморская"
                "bar" -> "Бар"
                "pub" -> "Паб"
                "cafe" -> "Кафе"
                "healthy" -> "ЗОЖ"
                "international" -> "Интернациональная"
                else -> t
            }
        }
        val tail = if (tags.size > 4) " +${tags.size - 4}" else ""
        return "Кухня: ${localized.joinToString(", ")}$tail"
    }

    private fun normalizeStatusRu(category: String?, text: String?): String {
        val c = category?.trim()?.uppercase()
        val t = text?.trim().orEmpty()
        return when (c) {
            "OPEN" -> "Открыто"
            "CLOSED" -> "Закрыто"
            "CLOSING" -> {
                val m = Regex("Closes in\\s+(\\d+)\\s+min", RegexOption.IGNORE_CASE)
                    .find(t)?.groupValues?.getOrNull(1)
                if (!m.isNullOrBlank()) "Закроется через $m мин" else "Скоро закроется"
            }
            else -> when {
                t.equals("Open Now", true) -> "Открыто"
                t.equals("Closed Now", true) -> "Закрыто"
                t.isNotBlank() -> t
                else -> "—"
            }
        }
    }

    // Эвристика priceTag -> диапазон среднего чека в ₽
    private fun buildAvgCheckRub(priceTag: String?): String {
        val tag = priceTag?.trim().orEmpty()
        val levels = parseDollarLevels(tag) ?: return "Средний чек: —"

        val ranges = mapOf(
            1 to (500 to 1200),
            2 to (1200 to 2500),
            3 to (2500 to 4500),
            4 to (4500 to 9000)
        )

        val (minL, maxL) = levels
        val a = ranges[minL]!!
        val b = ranges[maxL]!!
        val from = min(a.first, b.first)
        val to = max(a.second, b.second)

        return "Средний чек: ~ ${formatRub(from)}–${formatRub(to)} ₽"
    }

    private fun parseDollarLevels(tag: String): Pair<Int, Int>? {
        if (tag.isBlank()) return null
        val parts = tag.split("-").map { it.trim() }
        val left = parts.getOrNull(0)?.count { it == '$' } ?: 0
        if (left == 0) return null
        val right = parts.getOrNull(1)?.count { it == '$' } ?: left
        val minL = left.coerceIn(1, 4)
        val maxL = right.coerceIn(1, 4)
        return Pair(min(minL, maxL), max(minL, maxL))
    }

    private fun formatRub(v: Int): String =
        v.toString().reversed().chunked(3).joinToString(" ").reversed()
}