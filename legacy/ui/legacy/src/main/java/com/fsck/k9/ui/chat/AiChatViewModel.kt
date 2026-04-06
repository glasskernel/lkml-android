package com.fsck.k9.ui.chat

import androidx.lifecycle.viewModelScope
import com.fsck.k9.ui.chat.AiChatContract.Author
import com.fsck.k9.ui.chat.AiChatContract.ChatMessage
import com.fsck.k9.ui.chat.AiChatContract.Effect
import com.fsck.k9.ui.chat.AiChatContract.Event
import com.fsck.k9.ui.chat.AiChatContract.State
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import net.thunderbird.core.ui.contract.mvi.BaseViewModel

internal class AiChatViewModel(
    private val aiPatchAssistant: AiPatchAssistant,
    initialState: State = State(),
) : BaseViewModel<State, Event, Effect>(
    initialState = initialState,
),
    AiChatContract.ViewModel {

    private var nextMessageId = 0L

    override fun event(event: Event) {
        when (event) {
            is Event.OnPatchContextChange -> updateState { it.copy(patchContext = event.value) }
            is Event.OnDraftMessageChange -> updateState { it.copy(draftMessage = event.value) }
            Event.OnGenerateReviewClick -> generateReview()
            Event.OnSendMessageClick -> sendMessage()
            Event.OnClearConversationClick -> {
                updateState {
                    it.copy(
                        messages = emptyList<ChatMessage>().toPersistentList(),
                        errorMessage = null,
                    )
                }
            }

            Event.OnDismissErrorClick -> updateState { it.copy(errorMessage = null) }
        }
    }

    private fun generateReview() {
        val patchContext = state.value.patchContext.trim()
        if (state.value.isLoading) return

        if (patchContext.isEmpty()) {
            updateState { it.copy(errorMessage = "Paste a patch or thread into the context box first.") }
            return
        }

        updateState {
            it.copy(
                isLoading = true,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            runCatching {
                aiPatchAssistant.generateReviewDraft(patchContext)
            }.onSuccess { response ->
                appendAssistantMessage(
                    text = response.toReviewDraft(),
                    modelName = response.modelName,
                )
            }.onFailure { error ->
                updateState {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to generate an AI review.",
                    )
                }
            }
        }
    }

    private fun sendMessage() {
        val userMessage = state.value.draftMessage.trim()
        if (state.value.isLoading || userMessage.isEmpty()) return

        val conversation = state.value.messages
        val userChatMessage = ChatMessage(
            id = nextMessageId(),
            author = Author.USER,
            text = userMessage,
        )

        updateState {
            it.copy(
                draftMessage = "",
                isLoading = true,
                errorMessage = null,
                messages = (conversation + userChatMessage).toPersistentList(),
            )
        }

        viewModelScope.launch {
            runCatching {
                aiPatchAssistant.sendChatMessage(
                    patchContext = state.value.patchContext,
                    conversation = conversation.map { message ->
                        AiAssistantChatMessage(
                            role = when (message.author) {
                                Author.USER -> AiAssistantChatMessage.Role.USER
                                Author.ASSISTANT -> AiAssistantChatMessage.Role.ASSISTANT
                            },
                            text = message.text,
                        )
                    },
                    userMessage = userMessage,
                )
            }.onSuccess { response ->
                appendAssistantMessage(
                    text = response.text,
                    modelName = response.modelName,
                )
            }.onFailure { error ->
                updateState {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to send the AI chat message.",
                    )
                }
            }
        }
    }

    private fun appendAssistantMessage(text: String, modelName: String) {
        val assistantMessage = ChatMessage(
            id = nextMessageId(),
            author = Author.ASSISTANT,
            text = text,
            modelName = modelName,
        )

        updateState {
            it.copy(
                isLoading = false,
                messages = (it.messages + assistantMessage).toPersistentList(),
            )
        }
    }

    private fun nextMessageId(): Long {
        val currentId = nextMessageId
        nextMessageId += 1
        return currentId
    }
}
