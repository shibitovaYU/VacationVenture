// RecoEventSender.kt
import android.util.Log
import com.example.vacationventure.model.dto.recommendations.RecoEvent
import com.example.vacationventure.network.recommendations.RecoApi
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RecoEventSender(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val api: RecoApi = RecoNetwork.api
) {
    suspend fun send(event: RecoEvent) = withContext(Dispatchers.IO) {
        val user = auth.currentUser ?: run {
            Log.w("Reco", "No Firebase user, skip event")
            return@withContext
        }

        val token = user.getIdToken(false).await().token ?: run {
            Log.w("Reco", "No Firebase token, skip event")
            return@withContext
        }

        val resp = api.postEvent("Bearer $token", event)
        if (!resp.isSuccessful) {
            Log.e("Reco", "Event failed: ${resp.code()} ${resp.message()}")
        }
    }
}