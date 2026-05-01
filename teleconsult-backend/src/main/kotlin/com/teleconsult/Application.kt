package com.teleconsult

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Serializable
data class Doctor(val id: Int, val name: String, val specialty: String, val availability: String)

@Serializable
data class Appointment(val id: Int, val doctorName: String, val specialty: String, val time: String, val status: String)

object Doctors : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val specialty = varchar("specialty", 255)
    val availability = varchar("availability", 255)
    override val primaryKey = PrimaryKey(id)
}

object Appointments : Table() {
    val id = integer("id").autoIncrement()
    val doctorName = varchar("doctorName", 255)
    val specialty = varchar("specialty", 255)
    val time = varchar("time", 255)
    val status = varchar("status", 255)
    override val primaryKey = PrimaryKey(id)
}

fun main() {
    Database.connect("jdbc:h2:mem:teleconsult;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
    
    transaction {
        SchemaUtils.create(Doctors, Appointments)
        // Removed all hardcoded demo data as requested. The database now starts empty for production use.
    }

    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json()
        }
        install(WebSockets)
        install(CORS) {
            anyHost()
        }
        
        routing {
            get("/api/doctors") {
                val list = transaction {
                    Doctors.selectAll().map {
                        Doctor(it[Doctors.id], it[Doctors.name], it[Doctors.specialty], it[Doctors.availability])
                    }
                }
                call.respond(list)
            }

            // Endpoint to add REAL doctors
            post("/api/doctors") {
                val doctorParams = call.receive<Doctor>()
                transaction {
                    Doctors.insert {
                        it[name] = doctorParams.name
                        it[specialty] = doctorParams.specialty
                        it[availability] = doctorParams.availability
                    }
                }
                call.respond(mapOf("status" to "Success"))
            }

            get("/api/appointments/upcoming") {
                val appointment = transaction {
                    Appointments.selectAll().limit(1).map {
                        Appointment(it[Appointments.id], it[Appointments.doctorName], it[Appointments.specialty], it[Appointments.time], it[Appointments.status])
                    }.firstOrNull()
                }
                if (appointment != null) {
                    call.respond(appointment)
                } else {
                    call.respond(mapOf("error" to "No upcoming appointments"))
                }
            }

            // Endpoint to add REAL appointments
            post("/api/appointments") {
                val apptParams = call.receive<Appointment>()
                transaction {
                    Appointments.insert {
                        it[doctorName] = apptParams.doctorName
                        it[specialty] = apptParams.specialty
                        it[time] = apptParams.time
                        it[status] = apptParams.status
                    }
                }
                call.respond(mapOf("status" to "Success"))
            }

            val sessions = ConcurrentHashMap<String, DefaultWebSocketServerSession>()
            
            webSocket("/signaling/{userId}") {
                val userId = call.parameters["userId"] ?: return@webSocket
                sessions[userId] = this
                println("User $userId connected to signaling")
                try {
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            val parts = text.split("|", limit = 2)
                            if (parts.size == 2) {
                                val targetUser = parts[0]
                                val payload = parts[1]
                                println("Routing signal from $userId to $targetUser")
                                sessions[targetUser]?.send(Frame.Text("$userId|$payload"))
                            }
                        }
                    }
                } finally {
                    sessions.remove(userId)
                    println("User $userId disconnected")
                }
            }
        }
    }.start(wait = true)
}
