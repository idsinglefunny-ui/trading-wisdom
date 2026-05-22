package com.tradeyourplan.ui.quote

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.tradeyourplan.domain.model.Category
import com.tradeyourplan.domain.model.MarketType
import com.tradeyourplan.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQuoteScreen(
    viewModel: QuoteViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    var content by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(Category.DISCIPLINE) }
    var selectedMarketType by remember { mutableStateOf(MarketType.GENERAL) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showMarketTypeMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加语录") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 语录内容
            TYPOutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = "语录内容",
                placeholder = "输入交易智慧...",
                supportingText = if (content.length > 200) "已超过 200 字符" else "${content.length}/200",
                singleLine = false,
                maxLines = 5
            )

            // 分类选择
            ExposedDropdownMenuBox(
                expanded = showCategoryMenu,
                onExpandedChange = { showCategoryMenu = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("分类") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showCategoryMenu,
                    onDismissRequest = { showCategoryMenu = false }
                ) {
                    Category.values.toList().forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.displayName) },
                            onClick = {
                                selectedCategory = category
                                showCategoryMenu = false
                            }
                        )
                    }
                }
            }

            // 市场类型选择
            ExposedDropdownMenuBox(
                expanded = showMarketTypeMenu,
                onExpandedChange = { showMarketTypeMenu = it }
            ) {
                OutlinedTextField(
                    value = selectedMarketType.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("市场类型") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showMarketTypeMenu) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showMarketTypeMenu,
                    onDismissRequest = { showMarketTypeMenu = false }
                ) {
                    MarketType.values.toList().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.displayName) },
                            onClick = {
                                selectedMarketType = type
                                showMarketTypeMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // 保存按钮
            TYPButton(
                onClick = {
                    if (content.isNotBlank()) {
                        viewModel.addQuote(content, selectedCategory, selectedMarketType)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = content.isNotBlank(),
                text = "保存"
            )
        }
    }
}
