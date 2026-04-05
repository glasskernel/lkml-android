package com.fsck.k9.ui.lore

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

class LoreThreadActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val messageId = intent.getStringExtra(EXTRA_MESSAGE_ID) ?: return finish()
        
        // lore.kernel.org provides a threaded HTML view ending in /T/
        val url = "https://lore.kernel.org/all/$messageId/T/#u"

        setContent {
            LoreThreadScreen(
                url = url,
                onBack = { finish() }
            )
        }
    }

    companion object {
        private const val EXTRA_MESSAGE_ID = "message_id"

        fun createLaunchIntent(context: Context, messageId: String): Intent {
            return Intent(context, LoreThreadActivity::class.java).apply {
                putExtra(EXTRA_MESSAGE_ID, messageId.removePrefix("<").removeSuffix(">"))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoreThreadScreen(url: String, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lore Thread") },
                navigationIcon = {
                    // Simple back button implementation for now
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        Text("<-")
                    }
                }
            )
        }
    ) { paddingValues ->
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    // Optimize webview for reading
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    
                    webViewClient = WebViewClient()
                    loadUrl(url)
                }
            }
        )
    }
}
