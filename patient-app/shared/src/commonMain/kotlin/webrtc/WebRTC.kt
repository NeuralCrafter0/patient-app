package webrtc

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

expect class VideoTrack

@Composable
expect fun VideoRenderer(track: VideoTrack, modifier: Modifier = Modifier)

interface WebRTCManager {
    fun initialize()
    fun startLocalVideo()
    fun stopLocalVideo()
    fun getLocalVideoTrack(): VideoTrack?
}

@Composable
expect fun rememberWebRTCManager(): WebRTCManager
