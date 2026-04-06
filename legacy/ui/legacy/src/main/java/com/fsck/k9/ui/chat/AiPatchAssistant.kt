package com.fsck.k9.ui.chat

import com.fsck.k9.ui.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface AiPatchAssistant {
    suspend fun generateReviewDraft(patchContent: String): AiAssistantResponse

    suspend fun sendChatMessage(
        patchContext: String,
        conversation: List<AiAssistantChatMessage>,
        userMessage: String,
    ): AiAssistantResponse
}

data class AiAssistantResponse(
    val text: String,
    val modelName: String,
)

data class AiAssistantChatMessage(
    val role: Role,
    val text: String,
) {
    enum class Role {
        USER,
        ASSISTANT,
    }
}

internal class GeminiAiPatchAssistant : AiPatchAssistant {
    override suspend fun generateReviewDraft(patchContent: String): AiAssistantResponse {
        return generateText(
            prompt = AiPatchAssistantPromptBuilder.buildReviewPrompt(patchContent),
        )
    }

    override suspend fun sendChatMessage(
        patchContext: String,
        conversation: List<AiAssistantChatMessage>,
        userMessage: String,
    ): AiAssistantResponse {
        return generateText(
            prompt = AiPatchAssistantPromptBuilder.buildChatPrompt(
                patchContext = patchContext,
                conversation = conversation,
                userMessage = userMessage,
            ),
        )
    }

    private suspend fun generateText(prompt: String): AiAssistantResponse = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNullOrEmpty()) {
            throw Exception("GEMINI_API_KEY is not configured in local.properties.")
        }

        val modelsToTry = listOf(
            "gemini-3.1-pro-preview",
            "gemini-3.1-flash-lite-preview",
            "gemini-3-flash-preview",
            "gemini-1.5-flash",
        )

        var lastError: Exception? = null

        for (modelName in modelsToTry) {
            try {
                val model = GenerativeModel(modelName = modelName, apiKey = apiKey)
                val response = model.generateContent(prompt)
                val responseText = response.text

                if (responseText != null) {
                    return@withContext AiAssistantResponse(
                        text = responseText,
                        modelName = modelName,
                    )
                }

                val reason = response.candidates.firstOrNull()?.finishReason?.name ?: "UNKNOWN"
                lastError = Exception("Model $modelName returned no text. Reason: $reason")
            } catch (error: Exception) {
                lastError = Exception("Model $modelName failed: ${error.message}", error)
                val errorMessage = error.message.orEmpty()
                val shouldRetry =
                    errorMessage.contains("429") || errorMessage.contains("quota", ignoreCase = true)

                if (!shouldRetry) {
                    break
                }
            }
        }

        throw lastError ?: Exception("All models failed to generate a response.")
    }
}

fun AiAssistantResponse.toReviewDraft(): String {
    return """
        --- AI PATCH REVIEW (DRAFT, by $modelName) ---
        $text
        -------------------------------
    """.trimIndent()
}
