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

                val prompt = """
                    You are an expert Linux kernel maintainer. Your goal is to perform a deep, rigorous review of the proposed kernel patch provided below.
                    
                    ### CORE GUIDELINES
                    - Use the official technical patterns, architectural rules, and subsystem-specific guidelines (specifically @dt-bindings.md and @networking.md) as the absolute source of truth for identifying anti-patterns and violations.
                    - CRITICAL REVIEW DIRECTIVE: Do NOT dismiss concerns just because you assume the surrounding system or caller handles it perfectly. Do not be overly charitable to the existing code. If there is a missing initialization, an unhandled edge case, or a brittle logic flow, report it as a concern immediately. Assume the worst-case scenario where external inputs and caller states are malformed.
                    
                    ### REVIEW STAGES TO PERFORM
                    1. Analyze Commit Main Goal: Evaluate high-level intent, architectural flaws, UAPI breakages, and long-term maintainability.
                    2. High-Level Implementation Verification: Verify the code actually implements the claims, check for undocumented side-effects, missing callbacks/interfaces, and semantic/mathematical soundness.
                    3. Execution Flow Verification: Trace control flow for logic errors, NULL dereferences, unhandled error paths, and preprocessor/linker correctness.
                    
                    ### OUTPUT FORMAT
                    Format your feedback as a concise, professional draft reply to the mailing list. Group your findings by severity or category.
                    
                    Patch Content:
                    $patchContent
                """.trimIndent()
                
                val modelsToTry = listOf(
                    "gemini-3.1-pro-preview",
                    "gemini-3.1-flash-lite-preview",
                    "gemini-3-flash-preview",
                    "gemini-1.5-flash"
                )

                var lastError: Exception? = null
                var responseText: String? = null
                var successfulModel: String? = null

                for (modelName in modelsToTry) {
                    try {
                        val model = GenerativeModel(modelName = modelName, apiKey = apiKey)
                        val response = model.generateContent(prompt)
                        responseText = response.text
                        
                        if (responseText != null) {
                            successfulModel = modelName
                            break
                        } else {
                            // If text is null, the response might have been blocked (safety, etc.)
                            val reason = response.candidates.firstOrNull()?.finishReason?.name ?: "UNKNOWN"
                            lastError = Exception("Model $modelName returned no text. Reason: $reason")
                        }
                    } catch (e: Exception) {
                        lastError = Exception("Model $modelName failed: ${e.message}", e)
                        val errorMsg = e.message ?: ""
                        // If it's not a quota/rate limit error, don't bother trying other models
                        if (!errorMsg.contains("429") && !errorMsg.contains("quota", ignoreCase = true)) {
                            break
                        }
                    }
                }

                if (responseText == null) {
                    throw lastError ?: Exception("All models failed to generate a response.")
                }

                val reviewText = responseText ?: "No review generated."

                withContext(Dispatchers.Main) {
                    val draft = """
                        --- AI PATCH REVIEW (DRAFT, by $successfulModel) ---
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
