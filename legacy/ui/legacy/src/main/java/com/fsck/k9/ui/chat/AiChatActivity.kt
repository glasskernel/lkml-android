package com.fsck.k9.ui.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.fsck.k9.ui.chat.AiChatContract.Effect
import net.thunderbird.core.ui.contract.mvi.observe
import net.thunderbird.core.ui.theme.api.FeatureThemeProvider
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class AiChatActivity : AppCompatActivity() {
    private val themeProvider: FeatureThemeProvider by inject()
    private val viewModel: AiChatViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val (state, dispatch) = viewModel.observe { _: Effect -> }

            themeProvider.WithTheme {
                AiChatScreen(
                    state = state.value,
                    onEvent = dispatch,
                    onBack = { finish() },
                )
            }
        }
    }

    companion object {
        fun createLaunchIntent(context: Context): Intent {
            return Intent(context, AiChatActivity::class.java)
        }
    }
}
