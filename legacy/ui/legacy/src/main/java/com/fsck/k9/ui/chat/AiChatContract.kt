package com.fsck.k9.ui.chat

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import net.thunderbird.core.ui.contract.mvi.UnidirectionalViewModel

internal interface AiChatContract {
    interface ViewModel : UnidirectionalViewModel<State, Event, Effect>

    @Stable
    data class State(
        val patchContext: String = "",
        val draftMessage: String = "",
        val messages: ImmutableList<ChatMessage> = persistentListOf(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
    )

    data class ChatMessage(
        val id: Long,
        val author: Author,
        val text: String,
        val modelName: String? = null,
    )

    enum class Author {
        USER,
        ASSISTANT,
    }

    sealed interface Event {
        data class OnPatchContextChange(val value: String) : Event
        data class OnDraftMessageChange(val value: String) : Event
        data object OnGenerateReviewClick : Event
        data object OnSendMessageClick : Event
        data object OnClearConversationClick : Event
        data object OnDismissErrorClick : Event
    }

    sealed interface Effect
}
