package com.fsck.k9.ui.chat

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val chatUiModule = module {
    single<AiPatchAssistant> { GeminiAiPatchAssistant() }
    viewModel { AiChatViewModel(aiPatchAssistant = get()) }
}
