package com.capstone.toma

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.NoiseSuppressor
import android.util.Log
import kotlinx.coroutines.channels.Channel
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.concurrent.thread
import kotlin.math.sqrt

/**
 * CHANGED: openWakeWord migration - Low-latency Audio Capture Hub with VAD for Enrollment
 */
class AudioStreamManager {
    private val SAMPLE_RATE = 16000
    private val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    private val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    private val BUFFER_SIZE = 1280 // 80ms chunks for openWakeWord

    private var audioRecord: AudioRecord? = null
    private var isRunning = false
    
    // Channels
    val pcmChannel = Channel<ByteArray>(Channel.CONFLATED)
    val enrollmentChannel = Channel<ByteArray>(Channel.BUFFERED)

    // VAD & Enrollment State
    private var isEnrollmentMode = false
    private var energyThreshold = 500.0 // Adjust based on noise environment
    private var recordingBuffer = mutableListOf<Byte>()
    
    @SuppressLint("MissingPermission")
    fun startCapture() {
        if (isRunning) return
        
        val minBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        val record = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            minBufSize.coerceAtLeast(BUFFER_SIZE * 2)
        )

        if (record.state != AudioRecord.STATE_INITIALIZED) {
            Log.e("AudioStreamManager", "❌ AudioRecord initialization failed")
            record.release()
            return
        }

        audioRecord = record

        // Enable Hardware Noise Suppression if available
        if (NoiseSuppressor.isAvailable()) {
            NoiseSuppressor.create(audioRecord!!.audioSessionId)?.enabled = true
        }
        if (AcousticEchoCanceler.isAvailable()) {
            AcousticEchoCanceler.create(audioRecord!!.audioSessionId)?.enabled = true
        }

        isRunning = true
        audioRecord?.startRecording()

        thread(name = "TomaAudioThread") {
            val buffer = ByteArray(BUFFER_SIZE * 2) // 16-bit PCM (2 bytes per sample)
            while (isRunning) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                if (read > 0) {
                    val pcmData = buffer.copyOfRange(0, read)
                    pcmChannel.trySend(pcmData)
                    
                    if (isEnrollmentMode) {
                        processEnrollment(pcmData)
                    }
                }
            }
        }
    }

    fun setEnrollmentMode(enabled: Boolean) {
        isEnrollmentMode = enabled
        if (!enabled) recordingBuffer.clear()
    }

    private fun processEnrollment(pcmData: ByteArray) {
        // Simple RMS energy calculation (VAD)
        var sum = 0.0
        val shorts = ByteBuffer.wrap(pcmData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
        while (shorts.hasRemaining()) {
            val sample = shorts.get().toDouble()
            sum += sample * sample
        }
        val rms = sqrt(sum / (pcmData.size / 2))

        if (rms > energyThreshold) {
            // Speech detected: add to buffer
            recordingBuffer.addAll(pcmData.toList())
        } else if (recordingBuffer.size > SAMPLE_RATE * 2 * 0.5) { // At least 0.5s recorded
            // Silence detected after speech: sample complete
            enrollmentChannel.trySend(recordingBuffer.toByteArray())
            recordingBuffer.clear()
        }
    }

    fun stopCapture() {
        isRunning = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }
}
