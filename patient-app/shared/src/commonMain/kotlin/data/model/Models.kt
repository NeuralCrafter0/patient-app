package data.model

import kotlinx.serialization.Serializable

@Serializable
data class Doctor(
    val id: Int,
    val name: String,
    val specialty: String,
    val availability: String
)

@Serializable
data class Appointment(
    val id: Int,
    val doctorName: String,
    val specialty: String,
    val time: String,
    val status: String
)
