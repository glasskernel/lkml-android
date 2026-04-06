package com.fsck.k9.ui.chat

internal object AiPatchAssistantPromptBuilder {
    fun buildReviewPrompt(patchContent: String): String {
        return """
            ${buildSystemInstructions()}
            
            ### TASK
            Perform a deep review of the proposed patch below.
            
            ### REVIEW STAGES TO PERFORM
            1. Analyze Commit Main Goal: Evaluate high-level intent, architectural flaws, UAPI breakages, and long-term maintainability.
            2. High-Level Implementation Verification: Verify the code actually implements the claims, check for undocumented side-effects, missing callbacks/interfaces, and semantic/mathematical soundness.
            3. Execution Flow Verification: Trace control flow for logic errors, NULL dereferences, unhandled error paths, and preprocessor/linker correctness.
            
            ### OUTPUT FORMAT
            Format your feedback as a concise, professional draft reply to the mailing list. Group your findings by severity or category.
            
            ### PATCH CONTENT
            ${patchContent.ifBlank { "(no patch content provided)" }}
        """.trimIndent()
    }

    fun buildChatPrompt(
        patchContext: String,
        conversation: List<AiAssistantChatMessage>,
        userMessage: String,
    ): String {
        return """
            ${buildSystemInstructions()}
            
            ### TASK
            Continue a patch review chat with the user.
            - Ground your answer in the provided patch or thread context whenever possible.
            - If the available context is insufficient, explicitly say what is missing and ask for the specific diff hunk, error path, or email context you need.
            - Be direct, technically rigorous, and concise.
            - When you identify a problem, explain the impact and suggest a concrete fix when practical.
            
            ### PATCH OR THREAD CONTEXT
            ${patchContext.ifBlank { "(no patch or thread context provided yet)" }}
            
            ### CONVERSATION SO FAR
            ${formatConversation(conversation)}
            
            ### USER MESSAGE
            ${userMessage.ifBlank { "(empty user message)" }}
            
            ### RESPONSE FORMAT
            Reply in plain text as an expert Linux kernel patch reviewer. Prefer short paragraphs or a flat bullet list when it improves clarity.
        """.trimIndent()
    }

    private fun buildSystemInstructions(): String {
        return """
            You are an expert Linux kernel maintainer. Your goal is to perform a deep, rigorous review of proposed kernel patches and related discussion.
            
            ### CORE GUIDELINES
            - Use the official technical patterns, architectural rules, and subsystem-specific guidelines (specifically @dt-bindings.md and @networking.md) as the absolute source of truth for identifying anti-patterns and violations.
            - CRITICAL REVIEW DIRECTIVE: Do NOT dismiss concerns just because you assume the surrounding system or caller handles it perfectly. Do not be overly charitable to the existing code. If there is a missing initialization, an unhandled edge case, or a brittle logic flow, report it as a concern immediately. Assume the worst-case scenario where external inputs and caller states are malformed.
        """.trimIndent()
    }

    private fun formatConversation(conversation: List<AiAssistantChatMessage>): String {
        if (conversation.isEmpty()) {
            return "No previous conversation."
        }

        return buildString {
            conversation.forEach { message ->
                val speaker = when (message.role) {
                    AiAssistantChatMessage.Role.USER -> "User"
                    AiAssistantChatMessage.Role.ASSISTANT -> "Assistant"
                }

                appendLine("$speaker: ${message.text}")
            }
        }.trim()
    }
}
