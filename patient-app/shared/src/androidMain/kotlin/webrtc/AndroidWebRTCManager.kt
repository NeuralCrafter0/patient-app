package webrtc

import android.content.Context
import android.util.Log
import org.webrtc.*
import java.util.*

class AndroidWebRTCManager(private val context: Context) : WebRTCManager {
    private var factory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var videoSource: VideoSource? = null
    private var localVideoTrack: org.webrtc.VideoTrack? = null
    private var capturer: VideoCapturer? = null
    private val eglBase = EglBase.create()

    private val TAG = "WebRTCManager"

    override fun initialize() {
        Log.d(TAG, "Initializing WebRTC...")
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)

        val encoderFactory = DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true)
        val decoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext)

        factory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .createPeerConnectionFactory()
            
        Log.d(TAG, "Factory created.")
    }

    override fun startLocalVideo() {
        Log.d(TAG, "Starting local video...")
        videoSource = factory?.createVideoSource(false)
        capturer = createCameraCapturer()
        
        val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext)
        capturer?.initialize(surfaceTextureHelper, context, videoSource?.capturerObserver)
        capturer?.startCapture(1280, 720, 30)
        
        localVideoTrack = factory?.createVideoTrack("video_track_id", videoSource)
        Log.d(TAG, "Local video track created: $localVideoTrack")
    }

    override fun stopLocalVideo() {
        Log.d(TAG, "Stopping local video...")
        capturer?.stopCapture()
        capturer?.dispose()
        videoSource?.dispose()
        localVideoTrack?.dispose()
    }

    override fun getLocalVideoTrack(): VideoTrack? {
        return localVideoTrack?.let { VideoTrack(it) }
    }

    private fun createCameraCapturer(): VideoCapturer? {
        val enumerator = Camera2Enumerator(context)
        val deviceNames = enumerator.deviceNames
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                return enumerator.createCapturer(deviceName, null)
            }
        }
        // Fallback to any camera
        return enumerator.createCapturer(deviceNames[0], null)
    }

    // --- Signaling & PeerConnection Logic ---

    fun createPeerConnection(observer: PeerConnection.Observer) {
        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        peerConnection = factory?.createPeerConnection(rtcConfig, observer)
        
        localVideoTrack?.let {
            peerConnection?.addTrack(it, listOf("stream_id"))
        }
    }

    fun createOffer(sdpObserver: SdpObserver) {
        val constraints = MediaConstraints()
        peerConnection?.createOffer(sdpObserver, constraints)
    }

    fun setLocalDescription(sdp: SessionDescription, sdpObserver: SdpObserver) {
        peerConnection?.setLocalDescription(sdpObserver, sdp)
    }

    fun setRemoteDescription(sdp: SessionDescription, sdpObserver: SdpObserver) {
        peerConnection?.setRemoteDescription(sdpObserver, sdp)
    }

    fun addIceCandidate(candidate: IceCandidate) {
        peerConnection?.addIceCandidate(candidate)
    }
}
