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
                
                var response = try {
                    val primaryModel = GenerativeModel(
                        modelName = "gemini-3.1-pro-preview",
                        apiKey = apiKey
                    )
                    primaryModel.generateContent(prompt)
                } catch (e: Exception) {
                    if (e.message?.contains("429") == true) {
                        val backupModel = GenerativeModel(
                            modelName = "gemini-3-flash-preview",
                            apiKey = apiKey
                        )
                        backupModel.generateContent(prompt)
                    } else {
                        throw e
                    }
                }

                val reviewText = response.text ?: "No review generated."

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
