package com.capstone.toma.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.toma.OpenAiManager
import com.capstone.toma.VoiceRequestResult
import com.capstone.toma.storage.RecipeStorageRepository
import com.capstone.toma.ui.screen.AiChatUiState
import com.capstone.toma.ui.screen.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val openAiManager = OpenAiManager()
    private val recipeRepository = RecipeStorageRepository.getInstance(application)

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableStateFlow<Pair<String, String?>?>(null)
    val navigationEvent: StateFlow<Pair<String, String?>?> = _navigationEvent.asStateFlow()

    private val _errorEvent = MutableStateFlow<String?>(null)
    val errorEvent: StateFlow<String?> = _errorEvent.asStateFlow()

    private val timeFormat = SimpleDateFormat("a h:mm", Locale.KOREAN)

    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }

    fun clearErrorEvent() {
        _errorEvent.value = null
        _uiState.update { 
            it.copy(
                errorDialogMessage = null,
                isTyping = false 
            ) 
        }
    }

    fun onInputTextChange(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage(text: String? = null) {
        if (_uiState.value.isTyping) return // [추가] 분석 중 중복 전송 방지
        val messageText = text ?: _uiState.value.inputText
        if (messageText.isBlank()) return
        sendCustomMessage(messageText)
    }

    /**
     * 사용자에게 보여지는 텍스트와 실제 AI에게 전달하는 텍스트를 다르게 설정할 수 있습니다.
     */
    fun sendCustomMessage(displayText: String, hiddenPrompt: String? = null) {
        if (displayText.isBlank()) return

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = displayText,
            isUser = true,
            timestamp = getCurrentTime()
        )

        _uiState.update { 
            it.copy(
                messages = it.messages + userMessage,
                inputText = if (hiddenPrompt == null) "" else it.inputText,
                isTyping = true
            )
        }

        // 실제 AI에게는 hiddenPrompt가 있으면 그걸 보내고, 없으면 displayText를 보냄
        processAiResponse(hiddenPrompt ?: displayText)
    }

    /**
     * 분석 단계별로 메시지를 업데이트하며 최종 결과를 받아옵니다.
     */
    fun startLinkAnalysis(userDisplay: String, initialAiText: String, onAnalyze: suspend (updateStatus: (String) -> Unit) -> VoiceRequestResult) {
        if (_uiState.value.isTyping) return // [추가] 중복 분석 시작 방지
        val userMsgId = UUID.randomUUID().toString()
        val aiMsgId = UUID.randomUUID().toString()

        val userMessage = ChatMessage(id = userMsgId, text = userDisplay, isUser = true, timestamp = getCurrentTime())
        val aiPendingMessage = ChatMessage(id = aiMsgId, text = initialAiText, isUser = false, timestamp = getCurrentTime())

        _uiState.update { 
            it.copy(
                messages = it.messages + userMessage + aiPendingMessage,
                isTyping = true
            )
        }

        viewModelScope.launch {
            val result = onAnalyze { status ->
                // 중간 상태 업데이트
                _uiState.update { state ->
                    state.copy(
                        messages = state.messages.map { 
                            if (it.id == aiMsgId) it.copy(text = status) else it 
                        }
                    )
                }
            }

            // 최종 결과 반영
            when (result) {
                is VoiceRequestResult.Success -> {
                    val resolved = resolveRecipeIfNeeded(result)
                    val validation = validateRecipeResult(resolved)
                    val responseMessage = responseMessageFor(resolved, validation)

                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages.map { 
                                if (it.id == aiMsgId) it.copy(text = responseMessage) else it
                            },
                            isTyping = false
                        )
                    }
                    persistDraftAndNavigateIfReady(resolved, validation)
                }
                is VoiceRequestResult.Error -> {
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages.map { 
                                if (it.id == aiMsgId) it.copy(text = "죄송해요. 분석 중에 문제가 발생했어요. 😢") else it 
                            },
                            isTyping = false,
                            errorDialogMessage = result.message
                        )
                    }
                    _errorEvent.value = result.message
                }
            }
        }
    }

    fun addInitialMessages(userText: String, aiResponse: String) {
        if (_uiState.value.messages.isNotEmpty()) return

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = userText,
            isUser = true,
            timestamp = getCurrentTime()
        )
        val aiMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = aiResponse,
            isUser = false,
            timestamp = getCurrentTime()
        )

        _uiState.update { 
            it.copy(messages = it.messages + userMessage + aiMessage)
        }
    }

    private fun processAiResponse(userText: String) {
        // 대화 내역 추출 (text와 isUser 정보만 추출)
        val history = _uiState.value.messages.map { it.text to it.isUser }

        viewModelScope.launch {
            when (val result = openAiManager.processChatRequestSuspend(userText, history)) {
                is VoiceRequestResult.Success -> {
                    val resolved = resolveRecipeIfNeeded(result)
                    val validation = validateRecipeResult(resolved)
                    val aiMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        text = responseMessageFor(resolved, validation),
                        isUser = false,
                        timestamp = getCurrentTime()
                    )
                    _uiState.update {
                        it.copy(
                            messages = it.messages + aiMessage,
                            isTyping = false
                        )
                    }
                    persistDraftAndNavigateIfReady(resolved, validation)
                }
                is VoiceRequestResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isTyping = false,
                            errorDialogMessage = "네트워크 연결이 원활하지 않아요. 다시 시도해 주세요."
                        )
                    }
                    _errorEvent.value = result.message
                }
            }
        }
    }

    private suspend fun resolveRecipeIfNeeded(
        result: VoiceRequestResult.Success
    ): VoiceRequestResult.Success {
        val validation = validateRecipeResult(result)
        if (!result.isRecipeLike() || validation.isComplete || validation.keyword.isBlank()) {
            return result
        }

        val hiddenPrompt = buildRecipeCompletionPrompt(result, validation)
        val history = _uiState.value.messages.map { it.text to it.isUser }
        return when (val refined = openAiManager.processChatRequestSuspend(hiddenPrompt, history)) {
            is VoiceRequestResult.Success -> {
                val refinedValidation = validateRecipeResult(refined)
                if (refined.isRecipeLike() && refinedValidation.recipeDataJson != null) {
                    refined
                } else {
                    result
                }
            }
            is VoiceRequestResult.Error -> result
        }
    }

    private suspend fun persistDraftAndNavigateIfReady(
        result: VoiceRequestResult.Success,
        validation: RecipeValidation
    ) {
        if (!result.isRecipeLike()) return
        val recipeDataJson = validation.recipeDataJson ?: result.recipeData ?: return
        val keyword = validation.keyword.ifBlank { result.keyword }

        runCatching {
            recipeRepository.saveRecipeDraft(
                keyword = keyword,
                recipeDataJson = recipeDataJson,
                complete = validation.isComplete
            )
        }

        if (validation.isComplete) {
            _navigationEvent.value = keyword to recipeDataJson
        }
    }

    private fun responseMessageFor(
        result: VoiceRequestResult.Success,
        validation: RecipeValidation
    ): String {
        if (result.isRecipeLike() && !validation.isComplete) {
            val original = result.responseMessage.trim()
            if (original.isNotBlank() && !original.contains("시작할까요")) {
                return original
            }

            val missing = validation.missingFields.joinToString(", ") { it.toKoreanFieldName() }
            return "확인창으로 넘어가기 전에 $missing 정보가 더 필요해요. ${nextQuestionFor(validation.missingFields)}"
        }

        return result.responseMessage.ifBlank {
            if (result.isRecipeLike()) {
                "${validation.keyword.ifBlank { "선택한 메뉴" }} 레시피 정보를 정리했어요. 안내를 시작할까요?"
            } else {
                "좋아요. 이어서 도와드릴게요."
            }
        }
    }

    private fun buildRecipeCompletionPrompt(
        result: VoiceRequestResult.Success,
        validation: RecipeValidation
    ): String {
        return """
            [레시피 초안 보강 요청]
            사용자가 선택한 메뉴: ${validation.keyword}
            현재 recipe_data JSON: ${result.recipeData ?: "{}"}
            앱 검증에서 부족한 필드: ${validation.missingFields.joinToString(", ")}

            지침:
            1. 내부적으로 한 번 더 레시피를 구성해서 필수 필드를 채우세요.
            2. 일반적으로 알려진 메뉴라면 사용자에게 다시 묻지 말고 표준 레시피로 완성하세요.
            3. 알레르기, 식단 제한, 보유 재료처럼 사용자 답변 없이는 결정하면 위험한 정보가 필요할 때만 질문하세요.
            4. 필수 필드가 모두 있으면 type은 recipe_search, recipe_complete는 true로 반환하세요.
            5. 필수 필드가 여전히 부족하면 type은 recipe_draft, recipe_complete는 false로 반환하고 missing_fields에 부족한 필드를 넣으세요.
            6. response에는 내부 추론을 쓰지 말고, 완성 요약 또는 추가 질문 하나만 담으세요.
        """.trimIndent()
    }

    private fun validateRecipeResult(result: VoiceRequestResult.Success): RecipeValidation {
        val recipeData = result.recipeData ?: return RecipeValidation(
            keyword = result.keyword,
            recipeDataJson = null,
            isComplete = false,
            missingFields = listOf("recipe_data")
        )

        val json = runCatching { JSONObject(recipeData) }.getOrNull() ?: return RecipeValidation(
            keyword = result.keyword,
            recipeDataJson = null,
            isComplete = false,
            missingFields = listOf("recipe_data")
        )

        val ingredients = parseJsonStringArray(json, "ingredients")
            .filter { it.hasUsefulRecipeText() }
        val steps = parseJsonStringArray(json, "steps")
            .filter { it.hasUsefulRecipeText() }
        if (ingredients.isNotEmpty()) {
            json.put("ingredients", org.json.JSONArray(ingredients))
        }
        if (steps.isNotEmpty()) {
            json.put("steps", org.json.JSONArray(steps))
        }
        val title = json.optString("title").ifBlank { result.keyword }
        if (title.isNotBlank()) json.put("title", title)

        val guessedCategory = guessRecipeCategory(title, ingredients)
        if (json.optString("category").isBlank() && guessedCategory != "기타") {
            json.put("category", guessedCategory)
        }

        val servings = parseServings(json)
        if (servings > 0) {
            json.put("servings", servings)
        }

        val missing = buildList {
            if (title.isBlank()) add("title")
            if (json.optString("category").isBlank()) add("category")
            if (ingredients.size < 2) add("ingredients")
            if (steps.size < 2) add("steps")
            if (json.optString("difficulty").isBlank()) add("difficulty")
            if (json.optString("time").isBlank()) add("time")
            if (servings <= 0) add("servings")
        }.distinct()

        return RecipeValidation(
            keyword = title.ifBlank { result.keyword },
            recipeDataJson = json.toString(),
            isComplete = missing.isEmpty(),
            missingFields = if (missing.isEmpty()) emptyList() else missing
        )
    }

    private fun parseJsonStringArray(json: JSONObject, key: String): List<String> {
        val array = json.optJSONArray(key) ?: return emptyList()
        return List(array.length()) { index -> array.optString(index) }
            .flatMap { value ->
                if (key == "ingredients" && value.contains(",")) {
                    value.split(",").map { it.trim() }
                } else {
                    listOf(value)
                }
            }
            .filter { it.isNotBlank() }
    }

    private fun parseServings(json: JSONObject): Int {
        val raw = json.opt("servings") ?: return 0
        return when (raw) {
            is Number -> raw.toInt()
            is String -> Regex("\\d+").find(raw)?.value?.toIntOrNull() ?: 0
            else -> 0
        }
    }

    private fun guessRecipeCategory(title: String, ingredients: List<String>): String {
        val text = (title + " " + ingredients.joinToString(" ")).lowercase(Locale.KOREAN)
        return when {
            listOf("김치", "된장", "고추장", "불고기", "비빔", "찌개", "국", "밥", "전", "나물", "떡").any(text::contains) -> "한식"
            listOf("짜장", "짬뽕", "마라", "탕수", "중화", "볶음면").any(text::contains) -> "중식"
            listOf("파스타", "스테이크", "샐러드", "리조또", "피자", "수프", "크림").any(text::contains) -> "양식"
            listOf("초밥", "라멘", "우동", "돈카츠", "가츠", "소바").any(text::contains) -> "일식"
            listOf("떡볶이", "김밥", "라볶이", "튀김", "순대").any(text::contains) -> "분식"
            listOf("케이크", "쿠키", "라떼", "스무디", "디저트", "음료", "말차").any(text::contains) -> "디저트/음료"
            else -> "기타"
        }
    }

    private fun nextQuestionFor(missingFields: List<String>): String {
        return when {
            "ingredients" in missingFields -> "주재료나 피하고 싶은 재료가 있을까요?"
            "servings" in missingFields -> "몇 인분 기준으로 만들까요?"
            "category" in missingFields -> "한식, 중식, 양식 중 어떤 느낌으로 정리할까요?"
            else -> "원하는 기준이 있으면 한 가지만 알려주세요."
        }
    }

    private fun String.hasUsefulRecipeText(): Boolean {
        val normalized = trim().lowercase(Locale.KOREAN)
        return normalized.isNotBlank() &&
            normalized !in setOf("재료", "재료1", "재료2", "단계", "1단계", "2단계") &&
            !normalized.contains("정보 없음") &&
            !normalized.contains("준비되지")
    }

    private fun String.toKoreanFieldName(): String {
        return when (this) {
            "recipe_data" -> "레시피 데이터"
            "title" -> "요리명"
            "category" -> "분류"
            "ingredients" -> "재료"
            "steps" -> "조리 순서"
            "difficulty" -> "난이도"
            "time" -> "소요 시간"
            "servings" -> "인분"
            else -> this
        }
    }

    private fun VoiceRequestResult.Success.isRecipeLike(): Boolean {
        return requestType == "recipe_search" || requestType == "recipe_draft"
    }

    private data class RecipeValidation(
        val keyword: String,
        val recipeDataJson: String?,
        val isComplete: Boolean,
        val missingFields: List<String>
    )

    private fun getCurrentTime(): String = timeFormat.format(Date())
}
