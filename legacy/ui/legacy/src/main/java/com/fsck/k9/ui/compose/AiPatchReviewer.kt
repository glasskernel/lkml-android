package com.fsck.k9.ui.compose

import com.fsck.k9.ui.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Service responsible for interacting with the Gemini API to generate
 * an inline code review for a given patch email.
 */
class AiPatchReviewer {
    interface ReviewCallback {
        fun onReviewGenerated(reviewText: String)
        fun onError(error: Exception)
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    fun generateReviewDraftAsync(patchContent: String, callback: ReviewCallback) {
        scope.launch {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        callback.onError(Exception("GEMINI_API_KEY is not configured in local.properties."))
                    }
                    return@launch
                }

                val prompt = "Review the following Linux Kernel patch and point out any bugs, logical errors, or style issues. Keep the feedback concise and format it as a draft reply to the mailing list.\n\nPatch Content:\n$patchContent"
                
                val modelsToTry = listOf(
                    "gemini-3.1-pro-preview",
                    "gemini-3-flash-preview",
                    "gemini-1.5-flash"
                )

                var lastError: Exception? = null
                var responseText: String? = null

                for (modelName in modelsToTry) {
                    try {
                        val model = GenerativeModel(modelName = modelName, apiKey = apiKey)
                        val response = model.generateContent(prompt)
                        responseText = response.text
                        if (responseText != null) break
                    } catch (e: Exception) {
                        lastError = e
                        val errorMsg = e.message ?: ""
                        // If it's not a quota/rate limit error, don't bother trying other models
                        if (!errorMsg.contains("429") && !errorMsg.contains("quota", ignoreCase = true)) {
                            break
                        }
                    }
                }

                if (responseText == null) {
                    throw lastError ?: Exception("No review generated and no error captured.")
                }

                val reviewText = responseText ?: "No review generated."

                withContext(Dispatchers.Main) {
                    val draft = """
                        --- AI PATCH REVIEW (DRAFT) ---
                        $reviewText
                        -------------------------------
                    """.trimIndent()
                    callback.onReviewGenerated(draft)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onError(e)
                }
            }
        }
    }
}
