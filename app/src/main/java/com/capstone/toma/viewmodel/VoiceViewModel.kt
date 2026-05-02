package com.capstone.toma.viewmodel

import android.app.Application
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.toma.AudioStreamManager
import com.capstone.toma.BuildConfig
import com.capstone.toma.OpenAiRealtimeManager
import com.capstone.toma.TimerManager
import com.capstone.toma.TomaIntent
import com.capstone.toma.TomaIntentParser
import com.capstone.toma.VoiceUiState
import com.capstone.toma.WakeWordManager
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * CHANGED: openWakeWord migration - Integrated State Machine & Realtime API
 */
class VoiceViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow<VoiceUiState>(VoiceUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _intentEvent = kotlinx.coroutines.flow.MutableSharedFlow<TomaIntent>()
    val intentEvent = _intentEvent.asSharedFlow()

    private val audioStreamManager = AudioStreamManager()
    private val wakeWordManager = WakeWordManager(application) {
        onWakeWordDetected()
    }
    private val timerManager = TimerManager(application)
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
    
    // API Key from BuildConfig (set in build.gradle.kts)
    private val realtimeManager = OpenAiRealtimeManager(
        apiKey = BuildConfig.OPENAI_API_KEY,
        onResult = { jsonResponse -> handleAiIntent(jsonResponse) },
        onError = { error -> _uiState.value = VoiceUiState.Error(error) }
    )

    private var enrollmentCount = 0
    private val TOTAL_ENROLLMENT_COUNT = 30

    init {
        audioStreamManager.startCapture()
        realtimeManager.connect()
        observeAudioStream()
        observeEnrollmentStream()
    }

    private fun observeEnrollmentStream() {
        viewModelScope.launch {
            for (audioData in audioStreamManager.enrollmentChannel) {
                if (_uiState.value is VoiceUiState.Enrolling) {
                    uploadEnrollmentSample(audioData)
                }
            }
        }
    }

    fun startEnrollment() {
        enrollmentCount = 0
        _uiState.value = VoiceUiState.Enrolling(0)
        audioStreamManager.setEnrollmentMode(true)
    }

    private fun uploadEnrollmentSample(audioData: ByteArray) {
        val nextCount = enrollmentCount + 1
        val userId = "user_test_001" // TODO: Use real Auth ID
        val fileName = "me_${String.format("%03d", nextCount)}.wav"
        val storageRef = Firebase.storage.reference
            .child("users/$userId/recordings/$fileName")

        storageRef.putBytes(audioData)
            .addOnSuccessListener {
                enrollmentCount++
                _uiState.value = VoiceUiState.Enrolling(enrollmentCount)
                if (enrollmentCount >= TOTAL_ENROLLMENT_COUNT) {
                    completeEnrollment()
                }
            }
            .addOnFailureListener {
                Log.e("VoiceViewModel", "Upload failed for $fileName: ${it.message}")
            }
    }

    private fun completeEnrollment() {
        audioStreamManager.setEnrollmentMode(false)
        _uiState.value = VoiceUiState.Uploading
        
        // Polling for trained model (Simulation or actual backend check)
        checkAndDownloadModel()
    }

    private fun checkAndDownloadModel() {
        viewModelScope.launch {
            _uiState.value = VoiceUiState.Training
            val userId = "user_test_001"
            val modelRef = Firebase.storage.reference.child("users/$userId/models/hey_toma.onnx")
            
            var modelDownloaded = false
            var attempts = 0
            while (!modelDownloaded && attempts < 60) { // Poll for 5 minutes (5s * 60)
                delay(5000)
                attempts++
                
                modelRef.getBytes(10 * 1024 * 1024) // Max 10MB
                    .addOnSuccessListener { bytes ->
                        wakeWordManager.updateClassifierModel(bytes)
                        _uiState.value = VoiceUiState.Idle
                        modelDownloaded = true
                        Log.d("VoiceViewModel", "✅ Custom model downloaded and applied")
                    }
                    .addOnFailureListener {
                        Log.d("VoiceViewModel", "Waiting for model... (Attempt $attempts)")
                    }
                
                if (modelDownloaded) break
            }
            
            if (!modelDownloaded) {
                _uiState.value = VoiceUiState.Error("모델 학습 시간이 초과되었습니다. 나중에 다시 시도해주세요.")
                delay(3000)
                _uiState.value = VoiceUiState.Idle
            }
        }
    }

    private fun observeAudioStream() {
        viewModelScope.launch {
            for (pcmData in audioStreamManager.pcmChannel) {
                // 1. Always process for WakeWord if in IDLE
                if (_uiState.value == VoiceUiState.Idle) {
                    wakeWordManager.processFrame(pcmData)
                }
                
                // 2. Stream to OpenAI if in LISTENING
                if (_uiState.value == VoiceUiState.Listening) {
                    realtimeManager.sendAudio(pcmData)
                }
            }
        }
    }

    private fun onWakeWordDetected() {
        if (_uiState.value == VoiceUiState.Idle) {
            _uiState.value = VoiceUiState.Listening
            // Play earcon to notify user
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
            
            // Auto-timeout if no speech detected for 5 seconds
            viewModelScope.launch {
                delay(5000)
                if (_uiState.value == VoiceUiState.Listening) {
                    _uiState.value = VoiceUiState.Idle
                }
            }
        }
    }

    private fun handleAiIntent(jsonResponse: String) {
        val intent = TomaIntentParser.parse(jsonResponse)
        _uiState.value = VoiceUiState.Processing
        
        Log.d("VoiceViewModel", "Intent Parsed: $intent")

        viewModelScope.launch {
            _intentEvent.emit(intent)
        }

        when (intent) {
            is TomaIntent.SET_TIMER -> {
                timerManager.setTimer(intent.durationMin)
                _uiState.value = VoiceUiState.Result("${intent.durationMin}분 타이머를 맞췄어요.")
            }
            TomaIntent.NEXT_STEP -> {
                _uiState.value = VoiceUiState.Result("다음 단계로 넘어갑니다.")
                // TODO: Link with RecipeDetail screen action
            }
            TomaIntent.PREVIOUS_STEP -> {
                _uiState.value = VoiceUiState.Result("이전 단계로 돌아갑니다.")
            }
            TomaIntent.REPEAT_STEP -> {
                _uiState.value = VoiceUiState.Result("다시 읽어드릴게요.")
            }
            is TomaIntent.RECIPE_SEARCH -> {
                _uiState.value = VoiceUiState.Result("'${intent.keyword}' 레시피를 찾아볼게요.")
            }
            TomaIntent.CANCEL -> {
                _uiState.value = VoiceUiState.Idle
                return
            }
            else -> {
                _uiState.value = VoiceUiState.Error("명령을 이해하지 못했어요.")
            }
        }

        // Transition to SPEAKING then back to IDLE after a short delay
        viewModelScope.launch {
            _uiState.value = VoiceUiState.Speaking
            delay(3000)
            _uiState.value = VoiceUiState.Idle
        }
    }

    fun onMicClick() {
        // Manual trigger (optional, keeps current UI working)
        if (_uiState.value == VoiceUiState.Idle) {
            onWakeWordDetected()
        } else {
            _uiState.value = VoiceUiState.Idle
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioStreamManager.stopCapture()
        realtimeManager.disconnect()
        wakeWordManager.release()
    }
}
