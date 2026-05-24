package com.tradeyourplan.notification

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.lifecycleScope
import com.tradeyourplan.data.repository.QuoteRepository
import com.tradeyourplan.ui.theme.ThemeMode
import com.tradeyourplan.ui.theme.TradeYourPlanTheme
import com.tradeyourplan.utils.ShareHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class QuoteReminderActivity : ComponentActivity() {

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    @Inject
    lateinit var quoteRepository: QuoteRepository

    private val shareLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // Share completed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show over lock screen - MUST be done before setContentView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        // Additional flags for better lock screen behavior
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )

        // Make activity focusable and touchable
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val quoteText = intent.getStringExtra(EXTRA_QUOTE_TEXT) ?: "计划你的交易，交易你的计划。"

        setContent {
            val themeMode by dataStore.data.map { prefs ->
                val modeStr = prefs[stringPreferencesKey("theme_mode")] ?: "PROFESSIONAL_DARK"
                when (modeStr) {
                    "WARM_ENCOURAGING" -> ThemeMode.WARM_ENCOURAGING
                    "MINIMAL_LIGHT" -> ThemeMode.MINIMAL_LIGHT
                    else -> ThemeMode.PROFESSIONAL_DARK
                }
            }.collectAsState(initial = ThemeMode.PROFESSIONAL_DARK)

            TradeYourPlanTheme(themeMode = themeMode) {
                QuoteReminderScreen(
                    quoteText = quoteText,
                    themeMode = themeMode,
                    onDismiss = { finish() },
                    onFavoriteClick = { toggleFavorite(quoteText) },
                    onShareClick = { shareQuote(quoteText, themeMode) }
                )
            }
        }

        // Hide system bars AFTER setContent (translucent theme delays DecorView creation)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let { controller ->
                controller.hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    private fun toggleFavorite(quoteText: String) {
        lifecycleScope.launch {
            val quote = quoteRepository.getQuoteByContent(quoteText)
            quote?.let {
                quoteRepository.toggleFavorite(it.id)
            }
        }
    }

    private fun shareQuote(quoteText: String, themeMode: ThemeMode) {
        lifecycleScope.launch {
            try {
                // Step 1: Request dismiss keyguard if on lock screen
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                    if (keyguardManager.isKeyguardLocked) {
                        // Request dismiss keyguard
                        keyguardManager.requestDismissKeyguard(this@QuoteReminderActivity, null)
                        // Wait a bit for keyguard to be dismissed
                        delay(500)
                    }
                }

                // Step 2: Generate share image
                val shareIntent = ShareHelper.shareQuoteAsImage(
                    applicationContext,
                    quoteText,
                    themeMode
                )

                // Step 3: Show share chooser
                val chooser = ShareHelper.createShareChooser(shareIntent, "分享语录")
                shareLauncher.launch(chooser)

                // Step 4: Close the reminder activity after starting share
                delay(300)
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }

    companion object {
        const val EXTRA_QUOTE_TEXT = "quote_text"
    }
}

@Composable
private fun QuoteReminderScreen(
    quoteText: String,
    themeMode: ThemeMode,
    onDismiss: () -> Unit,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit
) {
    val context = LocalContext.current
    var isFavorite by remember { mutableStateOf(false) }

    // Check favorite status (simplified - in real app would flow from DB)
    LaunchedEffect(quoteText) {
        // This is a placeholder - actual implementation would use ViewModel
        // For now, we'll just check if it exists and could be favorited
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.92f))
            .clickable(
                onClick = onDismiss,
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        // Quote card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App title
                Text(
                    text = "交易智慧",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(20.dp))

                // Big quote text
                Text(
                    text = "「$quoteText」",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 28.sp,
                        lineHeight = 44.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Share button
                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "分享",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(Modifier.width(16.dp))

                    // Favorite button
                    IconButton(
                        onClick = {
                            onFavoriteClick()
                            isFavorite = !isFavorite
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "收藏",
                            tint = if (isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Dismiss hint
                Text(
                    text = "点击任意处关闭",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
