package data.remote

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import uniffi.teleconsult_sdk.TeleconsultSdk

class SignalingService(
    private val userId: String,
    private val rustSdk: TeleconsultSdk
) {
    private val client = HttpClient {
        install(WebSockets)
    }

    private var session: DefaultClientWebSocketSession? = null

    suspend fun connect() {
        try {
            session = client.webSocketSession("ws://10.218.91.187:8080/signaling/$userId")
            println("Connected to signaling as $userId")
        } catch (e: Exception) {
            println("Failed to connect to signaling: ${e.message}")
        }
    }

    suspend fun sendEncryptedSignal(targetId: String, payload: String) {
        val encrypted = rustSdk.encryptMessage(targetId, payload)
        // Format: "is_pre_key|ciphertext"
        val message = "${encrypted.isPreKey}|${encrypted.ciphertext}"
        session?.send("$targetId|$message")
    }

    fun observeSignals(): Flow<Pair<String, String>> {
        return session?.incoming?.receiveAsFlow()
            ?.filterIsInstance<Frame.Text>()
            ?.map { frame ->
                val text = frame.readText()
                val parts = text.split("|", limit = 2)
                val senderId = parts[0]
                val encryptedPayload = parts[1]
                
                val encryptedParts = encryptedPayload.split("|", limit = 2)
                val isPreKey = encryptedParts[0].toBoolean()
                val ciphertext = encryptedParts[1]
                
                val decrypted = rustSdk.decryptMessage(senderId, isPreKey, ciphertext)
                senderId to decrypted
            } ?: emptyFlow()
    }
}
