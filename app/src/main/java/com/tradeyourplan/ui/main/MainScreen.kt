// app/src/main/java/com/tradeyourplan/ui/main/MainScreen.kt
package com.tradeyourplan.ui.main

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tradeyourplan.ui.components.QuoteCard
import com.tradeyourplan.ui.components.TYPButton
import com.tradeyourplan.ui.theme.TradeYourPlanTheme

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToQuotes: () -> Unit = {},
    onNavigateToAlarms: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf("home") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("交易你的计划") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            BottomNavBar(
                items = listOf(
                    BottomNavItem("home", Icons.Default.Home, "首页"),
                    BottomNavItem("quotes", Icons.Default.FormatQuote, "语录"),
                    BottomNavItem("alarms", Icons.Default.Alarm, "闹钟"),
                    BottomNavItem("settings", Icons.Default.Settings, "设置")
                ),
                onItemClicked = { route ->
                    when (route) {
                        "home" -> selectedTab = "home"
                        "quotes" -> onNavigateToQuotes()
                        "alarms" -> onNavigateToAlarms()
                        "settings" -> onNavigateToSettings()
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                "home" -> HomeTab(
                    uiState = uiState,
                    onRefresh = { viewModel.loadRandomQuote() },
                    onFavorite = { id -> viewModel.toggleFavorite(id) }
                )
                "quotes" -> LaunchedEffect(Unit) { onNavigateToQuotes() }
                "alarms" -> LaunchedEffect(Unit) { onNavigateToAlarms() }
                "settings" -> LaunchedEffect(Unit) { onNavigateToSettings() }
            }
        }
    }
}

@Composable
private fun HomeTab(
    uiState: MainUiState,
    onRefresh: () -> Unit,
    onFavorite: (Long) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (uiState) {
            is MainUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            is MainUiState.Success -> {
                val quote = (uiState as MainUiState.Success).quote
                QuoteCard(
                    quote = quote,
                    modifier = Modifier.weight(1f),
                    onFavoriteClick = { onFavorite(quote.id) },
                    onShareClick = {
                        val shareText = """${quote.content}

—— 交易你的计划
#交易智慧 #投资语录""".trimIndent()
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "分享语录"))
                    }
                )
                TYPButton(
                    onClick = onRefresh,
                    modifier = Modifier.fillMaxWidth(),
                    icon = { Icon(Icons.Default.Refresh, null) },
                    text = "换一换"
                )
            }
            is MainUiState.Empty -> {
                Text("暂无语录，请先添加语录")
            }
        }
    }
}

@Composable
private fun BottomNavBar(
    items: List<BottomNavItem>,
    onItemClicked: (String) -> Unit = {}
) {
    var selectedItem by remember { mutableStateOf(0) }

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(item.label) },
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                    onItemClicked(item.route)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
