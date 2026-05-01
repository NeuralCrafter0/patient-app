package webrtc

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.webrtc.SurfaceViewRenderer
import org.webrtc.EglBase
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

actual class VideoTrack(val nativeTrack: org.webrtc.VideoTrack)

private val eglBase = EglBase.create()

@Composable
actual fun VideoRenderer(track: VideoTrack, modifier: Modifier) {
    AndroidView(
        factory = { context ->
            SurfaceViewRenderer(context).apply {
                init(eglBase.eglBaseContext, null)
                track.nativeTrack.addSink(this)
            }
        },
        modifier = modifier
    )
}

@Composable
actual fun rememberWebRTCManager(): WebRTCManager {
    val context = LocalContext.current
    return remember { AndroidWebRTCManager(context) }
}
