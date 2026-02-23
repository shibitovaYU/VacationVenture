// RecoNetwork.kt
import com.example.vacationventure.network.recommendations.RecoApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RecoNetwork {

    // Emulator: "http://10.0.2.2:8000/"
    // Phone:    "http://192.168.0.10:8000/"
    private const val BASE_URL = "http://10.0.2.2:8000/"

    private val okHttp by lazy {
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logger)
            .build()
    }

    val api: RecoApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RecoApi::class.java)
    }
}