package com.example.test2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.test2.data.database.CalculatorDatabase
import com.example.test2.data.repository.CalculationRepository
import com.example.test2.ui.components.*
import com.example.test2.ui.viewmodel.CalculatorViewModel
import com.example.test2.data.model.CalculationType

/**
 * メインの電卓画面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainCalculatorScreen(
    isDarkTheme: Boolean = true,
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val database = remember { CalculatorDatabase.getDatabase(context) }
    val repository = remember { CalculationRepository(database.calculationDao()) }
    val viewModel: CalculatorViewModel = viewModel { CalculatorViewModel(repository) }

    var selectedTab by remember { mutableIntStateOf(0) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // 電卓の名前を決定
    val calculatorName = when (selectedTab) {
        0 -> "一般電卓"
        1 -> "関数電卓"
        2 -> "単位換算"
        3 -> "通貨換算"
        4 -> "履歴"
        else -> "一般電卓"
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = if (isDarkTheme) Color(0xFF0D1117) else Color(0xFFF6F8FA),
                drawerContentColor = if (isDarkTheme) Color.White else Color.Black
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    // ヘッダー部分をよりおしゃれに
                    Column(
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        Text(
                            text = "Calculator",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isDarkTheme) Color(0xFFFF6B6B) else Color(0xFFDC2626)
                        )
                        Text(
                            text = "メニュー",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDarkTheme) Color(0xFF8B949E) else Color(0xFF656D76),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    NavigationDrawerItem(
                        label = { 
                            Text(
                                text = "一般電卓",
                                color = if (selectedTab == 0) {
                                    if (isDarkTheme) Color(0xFFFF6B6B) else Color(0xFFDC2626)
                                } else {
                                    if (isDarkTheme) Color(0xFFC9D1D9) else Color(0xFF24292F)
                                },
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (selectedTab == 0) FontWeight.Medium else FontWeight.Normal
                                )
                            )
                        },
                        selected = selectedTab == 0,
                        onClick = { 
                            selectedTab = 0
                            viewModel.setCalculationType(CalculationType.BASIC)
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = if (isDarkTheme) Color(0xFF2D1B1B) else Color(0xFFFEE2E2),
                            unselectedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    
                    NavigationDrawerItem(
                        label = { 
                            Text(
                                text = "関数電卓",
                                color = if (selectedTab == 1) {
                                    if (isDarkTheme) Color(0xFFFF6B6B) else Color(0xFFDC2626)
                                } else {
                                    if (isDarkTheme) Color(0xFFC9D1D9) else Color(0xFF24292F)
                                },
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (selectedTab == 1) FontWeight.Medium else FontWeight.Normal
                                )
                            )
                        },
                        selected = selectedTab == 1,
                        onClick = { 
                            selectedTab = 1
                            viewModel.setCalculationType(CalculationType.SCIENTIFIC)
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = if (isDarkTheme) Color(0xFF2D1B1B) else Color(0xFFFEE2E2),
                            unselectedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    
                    NavigationDrawerItem(
                        label = { 
                            Text(
                                text = "単位換算",
                                color = if (selectedTab == 2) {
                                    if (isDarkTheme) Color(0xFFFF6B6B) else Color(0xFFDC2626)
                                } else {
                                    if (isDarkTheme) Color(0xFFC9D1D9) else Color(0xFF24292F)
                                },
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (selectedTab == 2) FontWeight.Medium else FontWeight.Normal
                                )
                            )
                        },
                        selected = selectedTab == 2,
                        onClick = { 
                            selectedTab = 2
                            viewModel.setCalculationType(CalculationType.UNIT_CONVERSION)
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = if (isDarkTheme) Color(0xFF2D1B1B) else Color(0xFFFEE2E2),
                            unselectedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    // 通貨換算（単位換算と同じ画面を利用）
                    NavigationDrawerItem(
                        label = { 
                            Text(
                                text = "通貨換算",
                                color = if (selectedTab == 3) {
                                    if (isDarkTheme) Color(0xFFFF6B6B) else Color(0xFFDC2626)
                                } else {
                                    if (isDarkTheme) Color(0xFFC9D1D9) else Color(0xFF24292F)
                                },
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (selectedTab == 3) FontWeight.Medium else FontWeight.Normal
                                )
                            )
                        },
                        selected = selectedTab == 3,
                        onClick = { 
                            selectedTab = 3
                            // 通貨換算も履歴区別不要なら UNIT_CONVERSION を再利用
                            viewModel.setCalculationType(CalculationType.UNIT_CONVERSION)
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = if (isDarkTheme) Color(0xFF2D1B1B) else Color(0xFFFEE2E2),
                            unselectedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    
                    NavigationDrawerItem(
                        label = { 
                            Text(
                                text = "履歴",
                                color = if (selectedTab == 4) {
                                    if (isDarkTheme) Color(0xFFFF6B6B) else Color(0xFFDC2626)
                                } else {
                                    if (isDarkTheme) Color(0xFFC9D1D9) else Color(0xFF24292F)
                                },
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (selectedTab == 4) FontWeight.Medium else FontWeight.Normal
                                )
                            )
                        },
                        selected = selectedTab == 4,
                        onClick = { 
                            selectedTab = 4
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = if (isDarkTheme) Color(0xFF2D1B1B) else Color(0xFFFEE2E2),
                            unselectedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 20.dp),
                        color = if (isDarkTheme) Color(0xFF30363D) else Color(0xFFD1D9E0),
                        thickness = 1.dp
                    )
                    
                    NavigationDrawerItem(
                        label = { 
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "設定",
                                    tint = if (isDarkTheme) Color(0xFF8B949E) else Color(0xFF656D76),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "設定",
                                    color = if (isDarkTheme) Color(0xFFC9D1D9) else Color(0xFF24292F),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        },
                        selected = false,
                        onClick = { 
                            onSettingsClick()
                            scope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Color.Transparent,
                            unselectedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(if (isDarkTheme) Color(0xFF1C1C1E) else Color.White) // テーマに応じた背景色
                .padding(0.dp) // 16dpから0dpに変更（端を削除）
        ) {
            // ヘッダー部分（ハンバーガーメニューと電卓名）
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), // 内側のパディングのみ保持
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { 
                            scope.launch { 
                                if (drawerState.isClosed) {
                                    drawerState.open()
                                } else {
                                    drawerState.close()
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "メニュー",
                            tint = if (isDarkTheme) Color.White else Color.Black
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = calculatorName,
                        color = if (isDarkTheme) Color.White else Color.Black,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp)) // 16dpから8dpに縮小

            when (selectedTab) {
                0 -> BasicCalculatorTab(
                    viewModel,
                    onOpenUnitConversion = {
                        selectedTab = 2
                        viewModel.setCalculationType(com.example.test2.data.model.CalculationType.UNIT_CONVERSION)
                    }
                )
                1 -> ScientificCalculatorTab(viewModel)
                2 -> UnitConversionTab(isDarkTheme, viewModel) {
                    selectedTab = 0
                    viewModel.setCalculationType(com.example.test2.data.model.CalculationType.BASIC)
                }
                3 -> CurrencyConversionTab(isDarkTheme, viewModel) {
                    selectedTab = 0
                    viewModel.setCalculationType(com.example.test2.data.model.CalculationType.BASIC)
                }
                4 -> HistoryTab(viewModel)
            }
        }
    }
}

@Composable
fun BasicCalculatorTab(viewModel: CalculatorViewModel, onOpenUnitConversion: (() -> Unit)? = null) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp) // 0dpから4dpに間隔を少し開ける
    ) {
        // 表示部分（結果画面を少し大きく）
        CalculatorDisplay(
            displayText = viewModel.formattedDisplayText,
            expression = viewModel.displayExpression,
            expressionField = viewModel.expressionField.value,
            onExpressionEdited = { viewModel.onExpressionEdited(it) },
            onArrowKey = { direction -> viewModel.onArrowClicked(direction) },
            previewResult = viewModel.formattedPreviewResult,
            isResultShowing = viewModel.isResultShowing.value,
            modifier = Modifier.weight(0.4f) // 0.35fから0.4fに増加（結果画面を大きく）
        )

        // キーパッド（画面を埋めるように調整）
        BasicKeypad(
            viewModel = viewModel,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@Composable
fun ScientificCalculatorTab(viewModel: CalculatorViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 表示部分（関数電卓用）- より小さく
        CalculatorDisplay(
            displayText = viewModel.formattedDisplayText,
            expression = viewModel.displayExpression,
            expressionField = viewModel.expressionField.value,
            onExpressionEdited = { viewModel.onExpressionEdited(it) },
            onArrowKey = { direction -> viewModel.onArrowClicked(direction) },
            previewResult = viewModel.formattedPreviewResult,
            isResultShowing = viewModel.isResultShowing.value,
            modifier = Modifier.weight(0.2f) // 0.3fから0.2fに縮小
        )
        
        // 関数電卓キーパッド - より大きく
        ScientificKeypad(
            viewModel = viewModel,
            modifier = Modifier.weight(0.8f) // 0.7fから0.8fに拡大
        )
    }
}

@Composable
fun HistoryTab(viewModel: CalculatorViewModel) {
    val entries by viewModel.recentEntries.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "計算履歴",
                style = MaterialTheme.typography.headlineSmall
            )
            Button(
                onClick = { viewModel.clearAllHistory() }
            ) {
                Text("全削除")
            }
        }

        if (entries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "計算履歴がありません",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(entries) { entry ->
                    HistoryItem(
                        entry = entry,
                        onDelete = { viewModel.deleteHistoryEntry(entry.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun UnitConversionTab(
    isDarkTheme: Boolean = true,
    viewModel: CalculatorViewModel,
    onRequestOpenBasic: (() -> Unit)? = null
) {
    UnitConversionScreen(
        isDarkTheme = isDarkTheme,
        viewModel = viewModel,
        onRequestOpenBasic = onRequestOpenBasic
    )
}

@Composable
fun CurrencyConversionTab(
    isDarkTheme: Boolean = true,
    viewModel: CalculatorViewModel,
    onRequestOpenBasic: (() -> Unit)? = null
) {
    CurrencyConversionScreen(
        isDarkTheme = isDarkTheme,
        viewModel = viewModel,
        onRequestOpenBasic = onRequestOpenBasic
    )
}

/**
 * 数字を3桁区切りでフォーマットする
 */
private fun formatNumberWithCommas(number: String): String {
    if (number.isEmpty() || number == "Error" || number == "Division by Zero" || number == "Overflow Error" || 
        number == "数字から入力してください" || number == "Math Error") {
        return number
    }
    
    return try {
        // 小数点があるかチェック
        val parts = number.split(".")
        val integerPart = parts[0]
        val decimalPart = if (parts.size > 1) parts[1] else ""
        
        // 3桁区切りを適用
        val formattedInteger = integerPart.reversed().chunked(3).joinToString(",").reversed()
        
        if (decimalPart.isNotEmpty()) {
            "$formattedInteger.$decimalPart"
        } else {
            formattedInteger
        }
    } catch (e: Exception) {
        number
    }
}

@Preview(showBackground = true)
@Composable
fun MainCalculatorScreenPreview() {
    MaterialTheme {
        MainCalculatorScreen()
    }
}
