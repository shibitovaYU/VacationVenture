import android.util.Log
import com.example.vacationventure.model.FlightSegment
import com.example.vacationventure.model.dto.FlightFavorite.FlightFavoriteData
import com.example.vacationventure.model.dto.recommendations.*
import com.google.firebase.auth.FirebaseAuth

class RecoTracker(
    private val firebaseAuth: FirebaseAuth,
    private val recoSender: RecoEventSender
) {

    private fun buildFlightDetailUrl(segment: FlightSegment): String {
        val flightId = segment.thread.number.replace(" ", "-")
        return "https://travel.yandex.ru/avia/flights/$flightId/?when=${segment.start_date}"
    }

    suspend fun sendRecoEvent(eventType: EventType, fav: FlightFavoriteData) {
        val userId = firebaseAuth.currentUser?.uid ?: run {
            Log.w("RecoTracker", "No user, skip $eventType")
            return
        }

        val url = if (fav.detailUrl.isNotBlank()) fav.detailUrl
        else "https://travel.yandex.ru/avia/flights/${fav.number.replace(" ", "-")}/?when=${fav.startDate}"

        val departureTime = fav.departure.substring(11, 16)
        val arrivalTime = fav.arrival.substring(11, 16)
        val arrivalDate = fav.arrival.substring(0, 10)

        val durationHours = fav.duration / 3600
        val durationMinutes = (fav.duration % 3600) / 60
        val durationText = "Длительность: $durationHours ч $durationMinutes мин"

        val itemId = if (fav.itemId.isNotBlank()) fav.itemId
        else "${fav.threadUid}|${fav.startDate}"

        val event = RecoEvent(
            eventType = eventType,
            occurredAtMs = System.currentTimeMillis(),
            userId = userId,
            search = SearchContext(
                fromCode = fav.fromCode,
                toCode = fav.toCode,
                whenDate = fav.startDate
            ),
            item = ItemSnapshot(
                itemId = itemId,
                threadUid = fav.threadUid,
                title = fav.title,

                departureTime = departureTime,
                departureStation = "Аэропорт: ${fav.fromTitle}",
                departureDate = fav.startDate,

                arrivalTime = arrivalTime,
                arrivalStation = "Аэропорт: ${fav.toTitle}",
                arrivalDate = arrivalDate,

                durationText = durationText,
                detailUrl = url
            )
        )

        try {
            recoSender.send(event)
        } catch (e: Exception) {
            Log.e("RecoTracker", "Failed to send event=$eventType", e)
        }
    }

    suspend fun sendRecoEvent(eventType: EventType, segment: FlightSegment) {
        val userId = firebaseAuth.currentUser?.uid ?: run {
            Log.w("RecoTracker", "No user, skip $eventType")
            return
        }

        val url = buildFlightDetailUrl(segment)

        val departureTime = segment.departure.substring(11, 16)
        val arrivalTime = segment.arrival.substring(11, 16)
        val arrivalDate = segment.arrival.substring(0, 10)

        val durationHours = segment.duration / 3600
        val durationMinutes = (segment.duration % 3600) / 60
        val durationText = "Длительность: $durationHours ч $durationMinutes мин"

        val itemId = "${segment.thread.uid}|${segment.start_date}"

        val event = RecoEvent(
            eventType = eventType,
            occurredAtMs = System.currentTimeMillis(),
            userId = userId,
            search = SearchContext(
                fromCode = segment.from.code,
                toCode = segment.to.code,
                whenDate = segment.start_date
            ),
            item = ItemSnapshot(
                itemId = itemId,
                threadUid = segment.thread.uid,
                title = segment.thread.title,
                departureTime = departureTime,
                departureStation = "Аэропорт: ${segment.from.title}",
                departureDate = segment.start_date,
                arrivalTime = arrivalTime,
                arrivalStation = "Аэропорт: ${segment.to.title}",
                arrivalDate = arrivalDate,
                durationText = durationText,
                detailUrl = url
            )
        )

        try {
            recoSender.send(event)
        } catch (e: Exception) {
            Log.e("RecoTracker", "Failed to send event=$eventType", e)
        }
    }
}