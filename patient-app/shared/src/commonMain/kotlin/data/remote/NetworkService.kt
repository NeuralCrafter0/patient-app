package data.remote

import data.model.Doctor
import data.model.Appointment
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object NetworkService {
    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
    }

    private const val BASE_URL = "http://10.218.91.187:8080"

    suspend fun fetchDoctors(): List<Doctor> {
        return client.get("$BASE_URL/api/doctors").body()
    }

    suspend fun fetchUpcomingAppointment(): Appointment? {
        return try {
            client.get("$BASE_URL/api/appointments/upcoming").body()
        } catch (e: Exception) {
            println("Network Error: ${e.message}")
            null
        }
    }
}
