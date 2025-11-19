package ru.yulia.grades_android.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import ru.yulia.grades_android.data.repository.DefaultGradesRepository
import ru.yulia.grades_android.data.repository.GradesRepository
import ru.yulia.grades_android.ui.GradesViewModel
import java.util.concurrent.atomic.AtomicReference

object ServiceLocator {

    private const val DEFAULT_BASE_URL = "http://10.0.2.2:8000/"

    private val baseUrlRef = AtomicReference(DEFAULT_BASE_URL)
    private val json: Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()
    }

    @Volatile
    private var repository: GradesRepository? = null

    fun updateBaseUrl(newUrl: String) {
        if (newUrl.isBlank()) return
        val normalized = normalizeUrl(newUrl)
        if (baseUrlRef.getAndSet(normalized) == normalized) return
        repository = null
    }

    fun provideGradesRepository(): GradesRepository {
        val current = repository
        if (current != null) return current
        return synchronized(this) {
            val again = repository
            if (again != null) {
                again
            } else {
                val created = DefaultGradesRepository(
                    api = buildRetrofit(baseUrlRef.get()).create(),
                    json = json
                )
                repository = created
                created
            }
        }
    }

    fun provideGradesViewModelFactory(): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(GradesViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return GradesViewModel(provideGradesRepository()) as T
                }
                throw IllegalArgumentException("Unknown ViewModel: $modelClass")
            }
        }

    private fun buildRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .client(okHttpClient)
            .build()
    }

    private fun normalizeUrl(url: String): String {
        return if (url.endsWith("/")) url else "$url/"
    }
}
