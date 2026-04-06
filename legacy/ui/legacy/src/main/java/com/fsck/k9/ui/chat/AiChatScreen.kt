package com.fsck.k9.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.k9mail.core.ui.compose.designsystem.atom.CircularProgressIndicator
import app.k9mail.core.ui.compose.designsystem.atom.DividerHorizontal
import app.k9mail.core.ui.compose.designsystem.atom.Surface
import app.k9mail.core.ui.compose.designsystem.atom.button.ButtonFilled
import app.k9mail.core.ui.compose.designsystem.atom.button.ButtonOutlined
import app.k9mail.core.ui.compose.designsystem.atom.button.ButtonText
import app.k9mail.core.ui.compose.designsystem.atom.text.TextBodyMedium
import app.k9mail.core.ui.compose.designsystem.atom.text.TextLabelSmall
import app.k9mail.core.ui.compose.designsystem.atom.text.TextTitleMedium
import app.k9mail.core.ui.compose.designsystem.atom.text.TextTitleSmall
import app.k9mail.core.ui.compose.designsystem.atom.textfield.TextFieldOutlined
import app.k9mail.core.ui.compose.designsystem.organism.TopAppBarWithBackButton
import app.k9mail.core.ui.compose.designsystem.template.Scaffold
import com.fsck.k9.ui.R
import com.fsck.k9.ui.chat.AiChatContract.Author
import com.fsck.k9.ui.chat.AiChatContract.ChatMessage
import com.fsck.k9.ui.chat.AiChatContract.Event
import com.fsck.k9.ui.chat.AiChatContract.State
import net.thunderbird.core.ui.compose.designsystem.atom.icon.Icons
import net.thunderbird.core.ui.compose.theme2.MainTheme
import androidx.compose.ui.res.stringResource

@Composable
internal fun AiChatScreen(
    state: State,
    onEvent: (Event) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val messageListState = rememberLazyListState()

    LaunchedEffect(state.messages.size, state.isLoading) {
        val extraItemCount = if (state.isLoading) 1 else 0
        val lastIndex = state.messages.lastIndex + extraItemCount
        if (lastIndex >= 0) {
            messageListState.animateScrollToItem(lastIndex)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBarWithBackButton(
                title = stringResource(R.string.ai_chat_title),
                onBackClick = onBack,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding(),
        ) {
            ChatContextSection(
                patchContext = state.patchContext,
                isLoading = state.isLoading,
                onPatchContextChange = { onEvent(Event.OnPatchContextChange(it)) },
                onGenerateReviewClick = { onEvent(Event.OnGenerateReviewClick) },
                onClearConversationClick = { onEvent(Event.OnClearConversationClick) },
            )

            state.errorMessage?.let { errorMessage ->
                ErrorBanner(
                    message = errorMessage,
                    onDismiss = { onEvent(Event.OnDismissErrorClick) },
                )
            }

            if (state.messages.isEmpty() && !state.isLoading) {
                EmptyChatState(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = MainTheme.spacings.default),
                    verticalArrangement = Arrangement.spacedBy(MainTheme.spacings.default),
                    state = messageListState,
                ) {
                    items(
                        items = state.messages,
                        key = { it.id },
                    ) { message ->
                        ChatMessageBubble(message = message)
                    }

                    if (state.isLoading) {
                        item {
                            LoadingMessageBubble()
                        }
                    }
                }
            }

            DividerHorizontal()

            ChatComposer(
                draftMessage = state.draftMessage,
                isLoading = state.isLoading,
                onDraftMessageChange = { onEvent(Event.OnDraftMessageChange(it)) },
                onSendClick = { onEvent(Event.OnSendMessageClick) },
            )
        }
    }
}

@Composable
private fun ChatContextSection(
    patchContext: String,
    isLoading: Boolean,
    onPatchContextChange: (String) -> Unit,
    onGenerateReviewClick: () -> Unit,
    onClearConversationClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MainTheme.spacings.default, vertical = MainTheme.spacings.default),
        shape = RoundedCornerShape(20.dp),
        color = MainTheme.colors.surfaceContainerLow,
        tonalElevation = MainTheme.elevations.level1,
    ) {
        Column(
            modifier = Modifier.padding(MainTheme.spacings.double),
            verticalArrangement = Arrangement.spacedBy(MainTheme.spacings.default),
        ) {
            TextTitleMedium(text = stringResource(R.string.ai_chat_context_title))
            TextBodyMedium(text = stringResource(R.string.ai_chat_context_description))
            TextFieldOutlined(
                value = patchContext,
                onValueChange = onPatchContextChange,
                label = stringResource(R.string.ai_chat_context_label),
                isSingleLine = false,
                isEnabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MainTheme.spacings.default),
            ) {
                ButtonOutlined(
                    text = stringResource(R.string.ai_chat_generate_review_action),
                    onClick = onGenerateReviewClick,
                    enabled = !isLoading,
                    icon = Icons.Outlined.Description,
                    modifier = Modifier.weight(1f),
                )
                ButtonText(
                    text = stringResource(R.string.ai_chat_clear_chat_action),
                    onClick = onClearConversationClick,
                    enabled = !isLoading,
                )
            }
        }
    }
}

@Composable
private fun EmptyChatState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(MainTheme.spacings.double),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MainTheme.colors.surfaceContainerLow,
            tonalElevation = MainTheme.elevations.level1,
        ) {
            Column(
                modifier = Modifier.padding(MainTheme.spacings.double),
                verticalArrangement = Arrangement.spacedBy(MainTheme.spacings.default),
            ) {
                TextTitleSmall(text = stringResource(R.string.ai_chat_empty_title))
                TextBodyMedium(text = stringResource(R.string.ai_chat_empty_description))
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(message: ChatMessage) {
    val isUserMessage = message.author == Author.USER
    val containerColor = if (isUserMessage) {
        MainTheme.colors.secondaryContainer
    } else {
        MainTheme.colors.surfaceContainerHigh
    }
    val contentColor = if (isUserMessage) {
        MainTheme.colors.onSecondaryContainer
    } else {
        MainTheme.colors.onSurface
    }
    val metaColor = contentColor.copy(alpha = 0.8f)
    val horizontalAlignment = if (isUserMessage) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = horizontalAlignment,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(fraction = 0.92f),
            shape = RoundedCornerShape(20.dp),
            color = containerColor,
            contentColor = contentColor,
            tonalElevation = MainTheme.elevations.level1,
        ) {
            Column(
                modifier = Modifier.padding(MainTheme.spacings.double),
                verticalArrangement = Arrangement.spacedBy(MainTheme.spacings.half),
            ) {
                val labelRes = if (isUserMessage) R.string.ai_chat_user_label else R.string.ai_chat_assistant_label
                TextLabelSmall(
                    text = stringResource(labelRes),
                    color = metaColor,
                )
                TextBodyMedium(text = message.text)
                message.modelName?.takeIf { !isUserMessage }?.let { modelName ->
                    TextLabelSmall(
                        text = stringResource(R.string.ai_chat_model_caption, modelName),
                        color = metaColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingMessageBubble() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(fraction = 0.45f),
            shape = RoundedCornerShape(20.dp),
            color = MainTheme.colors.surfaceContainerHigh,
            tonalElevation = MainTheme.elevations.level1,
        ) {
            Row(
                modifier = Modifier.padding(MainTheme.spacings.double),
                horizontalArrangement = Arrangement.spacedBy(MainTheme.spacings.default),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator()
                TextBodyMedium(text = stringResource(R.string.ai_chat_loading_label))
            }
        }
    }
}

@Composable
private fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MainTheme.spacings.default)
            .padding(bottom = MainTheme.spacings.default),
        shape = RoundedCornerShape(16.dp),
        color = MainTheme.colors.errorContainer,
        contentColor = MainTheme.colors.onErrorContainer,
    ) {
        Row(
            modifier = Modifier.padding(MainTheme.spacings.default),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MainTheme.spacings.default),
        ) {
            TextBodyMedium(
                text = message,
                modifier = Modifier.weight(1f),
            )
            ButtonText(
                text = stringResource(R.string.done_action),
                onClick = onDismiss,
                color = MainTheme.colors.onErrorContainer,
            )
        }
    }
}

@Composable
private fun ChatComposer(
    draftMessage: String,
    isLoading: Boolean,
    onDraftMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(MainTheme.spacings.default),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(MainTheme.spacings.default),
    ) {
        TextFieldOutlined(
            value = draftMessage,
            onValueChange = onDraftMessageChange,
            label = stringResource(R.string.ai_chat_message_label),
            isSingleLine = false,
            isEnabled = !isLoading,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 56.dp, max = 140.dp),
        )
        ButtonFilled(
            text = stringResource(R.string.ai_chat_send_action),
            onClick = onSendClick,
            enabled = !isLoading && draftMessage.isNotBlank(),
            modifier = Modifier.padding(bottom = 4.dp),
        )
    }
}
