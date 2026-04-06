package com.fsck.k9.ui.compose

import com.fsck.k9.ui.chat.AiPatchAssistant
import com.fsck.k9.ui.chat.GeminiAiPatchAssistant
import com.fsck.k9.ui.chat.toReviewDraft
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Service responsible for interacting with the Gemini API to generate
 * an inline code review for a given patch email.
 */
class AiPatchReviewer(
    private val aiPatchAssistant: AiPatchAssistant = GeminiAiPatchAssistant(),
) {
    interface ReviewCallback {
        fun onReviewGenerated(reviewText: String)
        fun onError(error: Exception)
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    fun generateReviewDraftAsync(patchContent: String, callback: ReviewCallback) {
        scope.launch {
            try {
                val reviewText = aiPatchAssistant.generateReviewDraft(patchContent).toReviewDraft()

                withContext(Dispatchers.Main) {
                    callback.onReviewGenerated(reviewText)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onError(e)
                }
            }
        }
    }
}
