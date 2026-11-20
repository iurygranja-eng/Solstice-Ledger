package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.data.configureAppCheck
import com.example.notifications.RpgNotificationDispatcher
import com.example.ui.AppNavigation
import com.example.ui.theme.SolsticeLedgerTheme

class MainActivity : ComponentActivity() {
    private var pendingNotificationTarget by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure Firebase App Check with launch intent before UI or ViewModels
        configureAppCheck(this, intent)
        pendingNotificationTarget = intent?.getStringExtra(RpgNotificationDispatcher.EXTRA_OPEN_TARGET)

        enableEdgeToEdge()
        setContent {
            SolsticeLedgerTheme {
                AppNavigation(
                    deepLinkTarget = pendingNotificationTarget,
                    onConsumeDeepLink = { pendingNotificationTarget = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra(RpgNotificationDispatcher.EXTRA_OPEN_TARGET)?.let { target ->
            pendingNotificationTarget = target
        }
    }
}
