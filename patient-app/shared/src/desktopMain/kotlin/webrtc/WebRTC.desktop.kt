package webrtc

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

actual class VideoTrack

@Composable
actual fun VideoRenderer(track: VideoTrack, modifier: Modifier) {
}

@Composable
actual fun rememberWebRTCManager(): WebRTCManager {
    return remember {
        object : WebRTCManager {
            override fun initialize() {}
            override fun startLocalVideo() {}
            override fun stopLocalVideo() {}
            override fun getLocalVideoTrack(): VideoTrack? = null
        }
    }
}
