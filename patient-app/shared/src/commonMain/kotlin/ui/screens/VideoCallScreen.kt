package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import webrtc.VideoRenderer
import webrtc.VideoTrack
import webrtc.rememberWebRTCManager

class VideoCallScreen(val doctorName: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val webRTCManager = rememberWebRTCManager()
        var localTrack by remember { mutableStateOf<VideoTrack?>(null) }
        var callStatus by remember { mutableStateOf("Initializing Encryption...") }
        var isMicEnabled by remember { mutableStateOf(true) }
        var isCamEnabled by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            callStatus = "Olm Handshake..."
            webRTCManager.initialize()
            
            callStatus = "Camera Access..."
            webRTCManager.startLocalVideo()
            localTrack = webRTCManager.getLocalVideoTrack()
            
            callStatus = "Secure P2P Channel"
        }

        DisposableEffect(Unit) {
            onDispose {
                webRTCManager.stopLocalVideo()
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                
                // Real Video Layer
                localTrack?.let { track ->
                    VideoRenderer(
                        track = track,
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(strokeWidth = 4.dp, color = MaterialTheme.colorScheme.primary)
                }

                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                            )
                        )
                        .align(Alignment.TopCenter)
                )

                // Call Info
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(doctorName, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                    Text(callStatus, color = Color.LightGray, style = MaterialTheme.typography.labelMedium)
                }

                // Expressive M3 Controls
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 60.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = Color.DarkGray.copy(alpha = 0.8f),
                    tonalElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { isMicEnabled = !isMicEnabled },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (isMicEnabled) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                if (isMicEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = "Mic",
                                tint = Color.White
                            )
                        }

                        LargeFloatingActionButton(
                            onClick = { navigator.pop() },
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = Color.White,
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Icon(Icons.Default.CallEnd, contentDescription = "End Call", modifier = Modifier.size(32.dp))
                        }

                        IconButton(
                            onClick = { isCamEnabled = !isCamEnabled },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (isCamEnabled) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                if (isCamEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                contentDescription = "Camera",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
