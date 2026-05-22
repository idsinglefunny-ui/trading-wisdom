package com.tradeyourplan.ui.quote

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tradeyourplan.data.model.Quote
import com.tradeyourplan.domain.model.Category
import com.tradeyourplan.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteListScreen(
    viewModel: QuoteViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onAddQuote: () -> Unit = {}
) {
    val quotes by viewModel.quotes.collectAsState()
    val filterCategory by viewModel.filterCategory.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("语录列表") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onAddQuote) {
                        Icon(Icons.Default.Add, "添加语录")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (quotes.isEmpty()) {
            EmptyState(
                icon = {
                    Icon(
                        Icons.Default.FormatQuote,
                        null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                },
                title = "暂无语录",
                message = "点击右上角添加你的第一条交易智慧",
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 分类筛选
                item {
                    CategoryFilterRow(
                        selectedCategory = filterCategory,
                        onCategorySelected = { viewModel.setFilterCategory(it) }
                    )
                }

                // 语录列表
                items(quotes) { quote ->
                    QuoteCard(
                        quote = quote,
                        onFavoriteClick = { viewModel.toggleFavorite(quote.id) },
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
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilterRow(
    selectedCategory: Category?,
    onCategorySelected: (Category?) -> Unit
) {
    val categories = listOf(null) + Category.entries

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        category?.displayName ?: "全部",
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                shape = MaterialTheme.shapes.small
            )
        }
    }
}
