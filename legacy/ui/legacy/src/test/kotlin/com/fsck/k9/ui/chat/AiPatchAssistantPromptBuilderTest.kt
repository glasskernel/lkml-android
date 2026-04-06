package com.fsck.k9.ui.chat

import assertk.assertThat
import assertk.assertions.contains
import kotlin.test.Test

internal class AiPatchAssistantPromptBuilderTest {
    @Test
    fun `review prompt should include patch content and maintainer guidance`() {
        val prompt = AiPatchAssistantPromptBuilder.buildReviewPrompt(
            patchContent = "diff --git a/file.c b/file.c",
        )

        assertThat(prompt).contains("expert Linux kernel maintainer")
        assertThat(prompt).contains("diff --git a/file.c b/file.c")
        assertThat(prompt).contains("Format your feedback as a concise, professional draft reply")
    }

    @Test
    fun `chat prompt should include context conversation and user message`() {
        val prompt = AiPatchAssistantPromptBuilder.buildChatPrompt(
            patchContext = "Subject: [PATCH] fix race",
            conversation = listOf(
                AiAssistantChatMessage(
                    role = AiAssistantChatMessage.Role.USER,
                    text = "Does this handle the error path?",
                ),
                AiAssistantChatMessage(
                    role = AiAssistantChatMessage.Role.ASSISTANT,
                    text = "No, the cleanup path still leaks the reference.",
                ),
            ),
            userMessage = "What should the fix look like?",
        )

        assertThat(prompt).contains("Subject: [PATCH] fix race")
        assertThat(prompt).contains("User: Does this handle the error path?")
        assertThat(prompt).contains("Assistant: No, the cleanup path still leaks the reference.")
        assertThat(prompt).contains("What should the fix look like?")
    }

    @Test
    fun `review draft formatter should include model name`() {
        val formatted = AiAssistantResponse(
            text = "Looks good overall.",
            modelName = "gemini-3.1-pro-preview",
        ).toReviewDraft()

        assertThat(formatted).contains("gemini-3.1-pro-preview")
        assertThat(formatted).contains("Looks good overall.")
    }
}
