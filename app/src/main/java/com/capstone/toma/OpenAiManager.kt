package com.capstone.toma

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

class OpenAiManager {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val apiKey = BuildConfig.OPENAI_API_KEY

    /**
     * [1단계: STT] 음성을 텍스트로 변환
     * prompt를 추가하여 요리 관련 단어 인식률을 높임
     */
    fun transcribeAudio(audioFile: File, onResult: (String?) -> Unit) {
        if (!audioFile.exists()) {
            onResult(null)
            return
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", audioFile.name, audioFile.asRequestBody("audio/mpeg".toMediaType()))
            .addFormDataPart("model", "whisper-1")
            .addFormDataPart("language", "ko")
            // 요리 관련 컨텍스트를 주어 인식률 향상
            .addFormDataPart("prompt", "요리, 레시피, 식재료, 조리법, 토마, TOMA, 주방 어시스턴트")
            .build()

        val request = Request.Builder()
            .url("https://api.openai.com/v1/audio/transcriptions")
            .header("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResult(null)
            }
            override fun onResponse(call: Call, response: Response) {
                val resBody = response.body?.string()
                if (response.isSuccessful && resBody != null) {
                    val text = JSONObject(resBody).optString("text")
                    onResult(text)
                } else {
                    onResult(null)
                }
            }
        })
    }

    /**
     * Coroutine-friendly suspend version of processChatRequest
     */
    suspend fun processChatRequestSuspend(
        userText: String,
        history: List<Pair<String, Boolean>>
    ): VoiceRequestResult = suspendCancellableCoroutine { continuation ->
        processChatRequest(userText, history) { result ->
            if (continuation.isActive) {
                continuation.resume(result)
            }
        }
    }

    /**
     * [2단계: LLM] 요리 전문 AI 정체성 강화
     */
    fun processChatRequest(
        userText: String,
        history: List<Pair<String, Boolean>>,
        onResult: (VoiceRequestResult) -> Unit
    ) {
        val systemPrompt = """
            당신은 요리 전문 AI 어시스턴트 '토마(TOMA)'입니다.
            당신의 유일한 목적은 사용자의 요리를 돕는 것입니다. 
            
            [엄격한 정체성 규칙]
            1. 요리, 레시피, 식재료, 식단과 관련 없는 질문(연예, 노래, 정치 등)에는 절대 대답하지 마세요.
            2. 관련 없는 요청이 오면 "저는 요리 도우미라서 그건 잘 몰라요. 대신 맛있는 레시피를 찾아드릴까요?"와 같이 답변하세요.
            3. 사용자가 메뉴를 언급하면 먼저 내부적으로 레시피 초안을 구성하고, 화면 전환에 필요한 구조화 데이터를 완성하세요.
            4. 내부 추론 과정은 사용자에게 노출하지 말고, 최종 JSON의 response에는 친근한 요약 또는 필요한 추가 질문만 담으세요.
            5. [링크 분석 특화 지침]:
               - 콘텐츠 성격 판별: recipe(조리), guide(팁/노하우), article(정보), non_actionable(잡담/광고) 중 하나로 분류하세요.
               - 본문 정제: 인사말, 후기, 광고, 협찬 문구 등은 모두 제거하고 실제 도움되는 '실전 정보'만 남기세요.
               - 정보 추출: 제목, 한 줄 요약, 핵심 포인트, 재료, 도구, 시간, 난이도를 정확히 추출하세요. 원문에 없는 수치는 추정하지 마세요.
               - 단계 재구성: 사용자가 바로 따라할 수 있게 행동 중심의 단계형 가이드로 다시 작성하세요.

            [레시피 데이터 완성 규칙]
            - 특정 메뉴가 확정되면 recipe_data를 먼저 채우세요.
            - 일반적으로 알려진 메뉴라면 재료, 분류, 시간, 난이도, 인분, 조리 단계를 스스로 구성하세요.
            - 사용자의 취향, 보유 재료, 알레르기, 조리 도구처럼 실제 결과가 크게 달라지는 정보가 부족할 때만 추가 질문을 하세요.
            - recipe_data 필수 필드: title, category, ingredients, steps, difficulty, time, servings.
            - ingredients는 최소 2개 이상, steps는 최소 2단계 이상이어야 합니다.
            - 필수 필드가 완성되지 않았으면 type을 recipe_search로 쓰지 마세요. type은 recipe_draft로 두고 missing_fields에 부족한 필드를 넣으세요.
            - type이 recipe_search인 경우에만 recipe_complete를 true로 설정하세요.
            
            [응답 가이드라인]
            - "레시피 안내를 시작할까요?"라는 문구는 recipe_data가 완성되어 recipe_search를 반환할 때만 포함하세요.
            - recipe_search의 response에는 선택한 요리와 핵심 재료를 짧게 언급한 뒤 안내 시작 여부를 물으세요.
            - recipe_draft일 때는 확인창으로 넘어가지 않으므로, 부족한 정보를 채우기 위한 질문을 하나만 하세요.
            - 항상 친근하고 정중한 말투를 사용하세요.
            
            [응답 형식 (JSON)]
            {
              "type": "recipe_search", "recipe_draft", "chat", 또는 "menu_recommend",
              "keyword": "결정된 요리 이름 또는 검색어",
              "response": "사용자에게 줄 대답",
              "recipe_complete": true 또는 false,
              "missing_fields": ["부족한 필드명"],
              "recipe_data": {
                "title": "요리명",
                "category": "한식/중식/양식/일식/분식/디저트/음료/기타",
                "ingredients": ["재료1 (용량)", "재료2 (용량)"],
                "steps": ["1단계 설명", "2단계 설명"],
                "difficulty": "쉬움/보통/어려움",
                "time": "예상 소요 시간 (예: 20분)",
                "servings": 2,
                "image_url": "이미지 URL (있을 경우)"
              } (recipe_search 또는 recipe_draft일 경우 포함)
            }
        """.trimIndent()

        val messages = org.json.JSONArray().apply {
            put(JSONObject().apply { put("role", "system"); put("content", systemPrompt) })
            history.takeLast(10).forEach { (text, isUser) ->
                put(JSONObject().apply { 
                    put("role", if (isUser) "user" else "assistant")
                    put("content", text)
                })
            }
            put(JSONObject().apply { put("role", "user"); put("content", userText) })
        }

        val json = JSONObject().apply {
            put("model", "gpt-4o-mini")
            put("messages", messages)
            put("temperature", 0.5) // 정체성 유지를 위해 조금 더 일관성 있게 조절
            put("response_format", JSONObject().apply { put("type", "json_object") })
        }

        val requestBody = RequestBody.create("application/json".toMediaType(), json.toString())
        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResult(VoiceRequestResult.Error("네트워크 오류가 발생했습니다."))
            }
            override fun onResponse(call: Call, response: Response) {
                val resBody = response.body?.string()
                if (response.isSuccessful && resBody != null) {
                    try {
                        val resultJson = JSONObject(JSONObject(resBody).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content"))
                        onResult(VoiceRequestResult.Success(
                            requestType = resultJson.optString("type", "chat"),
                            keyword = resultJson.optString("keyword", ""),
                            responseMessage = resultJson.optString("response", ""),
                            recipeData = normalizeRecipeData(resultJson),
                            recipeComplete = resultJson.optBoolean("recipe_complete", false),
                            missingFields = parseStringArray(resultJson, "missing_fields")
                        ))
                    } catch (e: Exception) {
                        onResult(VoiceRequestResult.Error("분석 오류가 발생했습니다."))
                    }
                } else {
                    onResult(VoiceRequestResult.Error("API 응답 에러"))
                }
            }
        })
    }

    fun processVoiceRequest(userText: String, onResult: (VoiceRequestResult) -> Unit) {
        processChatRequest(userText, emptyList(), onResult)
    }

    private fun normalizeRecipeData(resultJson: JSONObject): String? {
        val recipeJson = resultJson.optJSONObject("recipe_data") ?: return null
        val keyword = resultJson.optString("keyword", "")
        if (recipeJson.optString("title").isBlank() && keyword.isNotBlank()) {
            recipeJson.put("title", keyword)
        }
        return recipeJson.toString()
    }

    private fun parseStringArray(json: JSONObject, key: String): List<String> {
        val array = json.optJSONArray(key) ?: return emptyList()
        return List(array.length()) { index -> array.optString(index) }
            .filter { it.isNotBlank() }
    }

    fun analyzeIntent(userText: String, onResult: (String?) -> Unit) {
        val systemPrompt = "요리 단계 진행 의도 분석: [다음, 이전, 타이머, 재료확인, 알수없음] 중 하나만 응답."
        val json = JSONObject().apply {
            put("model", "gpt-4o-mini")
            put("messages", org.json.JSONArray().apply {
                put(JSONObject().apply { put("role", "system"); put("content", systemPrompt) })
                put(JSONObject().apply { put("role", "user"); put("content", userText) })
            })
            put("temperature", 0.1)
        }

        val requestBody = RequestBody.create("application/json".toMediaType(), json.toString())
        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { onResult("에러") }
            override fun onResponse(call: Call, response: Response) {
                val resBody = response.body?.string()
                if (response.isSuccessful && resBody != null) {
                    val content = JSONObject(resBody).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content").trim()
                    onResult(content)
                } else { onResult("에러") }
            }
        })
    }

    /**
     * [3단계: Vision] 이미지 기반 레시피 분석
     */
    suspend fun analyzeRecipeImageSuspend(
        context: Context,
        imageUri: String
    ): VoiceRequestResult = suspendCancellableCoroutine { continuation ->
        try {
            val uri = Uri.parse(imageUri)
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream) ?: throw Exception("이미지를 불러올 수 없습니다.")

            // 이미지 크기 최적화 (API 비용 및 속도)
            val resizedBitmap = if (bitmap.width > 1024 || bitmap.height > 1024) {
                val scale = 1024f / Math.max(bitmap.width, bitmap.height)
                Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
            } else bitmap

            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

            val systemPrompt = """
                당신은 요리 사진 분석 전문가 '토마'입니다.
                이미지를 분석하여 다음 중 하나를 수행하세요:
                1. 완성된 요리 사진이라면: 어떤 요리인지 맞히고, 해당 요리의 일반적인 레시피를 생성하세요.
                2. 레시피 텍스트 사진이라면: 텍스트를 OCR하여 구조화된 레시피로 만드세요.
                
                반드시 다음 JSON 형식으로 응답하세요:
                {
                  "type": "recipe_search",
                  "keyword": "요리명",
                  "response": "사진을 분석해보니 [요리명]이네요. 재료와 조리 정보를 정리했어요. 레시피 안내를 시작할까요?",
                  "recipe_complete": true,
                  "missing_fields": [],
                  "recipe_data": {
                    "title": "요리명",
                    "category": "한식/중식/양식/일식/분식/디저트/음료/기타",
                    "ingredients": ["재료1 (용량)", "재료2 (용량)"],
                    "steps": ["1단계 설명", "2단계 설명"],
                    "difficulty": "쉬움/보통/어려움",
                    "time": "소요 시간",
                    "servings": 2
                  }
                }
            """.trimIndent()

            val json = JSONObject().apply {
                put("model", "gpt-4o") // Vision을 위해 gpt-4o 사용
                put("messages", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", org.json.JSONArray().apply {
                            put(JSONObject().apply { put("type", "text"); put("text", systemPrompt) })
                            put(JSONObject().apply {
                                put("type", "image_url")
                                put("image_url", JSONObject().apply {
                                    put("url", "data:image/jpeg;base64,$base64Image")
                                })
                            })
                        })
                    })
                })
                put("response_format", JSONObject().apply { put("type", "json_object") })
            }

            val requestBody = RequestBody.create("application/json".toMediaType(), json.toString())
            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer $apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (continuation.isActive) continuation.resume(VoiceRequestResult.Error("네트워크 연결 실패: ${e.message}"))
                }
                override fun onResponse(call: Call, response: Response) {
                    val resBody = response.body?.string()
                    if (response.isSuccessful && resBody != null) {
                        try {
                            val content = JSONObject(resBody).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
                            val resultJson = JSONObject(content)
                            if (continuation.isActive) continuation.resume(VoiceRequestResult.Success(
                                requestType = resultJson.optString("type", "recipe_search"),
                                keyword = resultJson.optString("keyword", "요리"),
                                responseMessage = resultJson.optString("response", ""),
                                recipeData = normalizeRecipeData(resultJson),
                                recipeComplete = resultJson.optBoolean("recipe_complete", false),
                                missingFields = parseStringArray(resultJson, "missing_fields")
                            ))
                        } catch (e: Exception) {
                            if (continuation.isActive) continuation.resume(VoiceRequestResult.Error("결과 분석 실패: ${e.message}"))
                        }
                    } else {
                        if (continuation.isActive) continuation.resume(VoiceRequestResult.Error("API 응답 실패 (코드: ${response.code})"))
                    }
                }
            })
        } catch (e: Exception) {
            if (continuation.isActive) continuation.resume(VoiceRequestResult.Error("이미지 처리 실패: ${e.message}"))
        }
    }
}

sealed class VoiceRequestResult {
    data class Success(
        val requestType: String,
        val keyword: String,
        val responseMessage: String,
        val recipeData: String? = null,
        val recipeComplete: Boolean = false,
        val missingFields: List<String> = emptyList()
    ) : VoiceRequestResult()
    data class Error(val message: String) : VoiceRequestResult()
}
