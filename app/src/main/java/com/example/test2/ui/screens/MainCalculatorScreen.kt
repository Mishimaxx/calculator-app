package com.example.test2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val database = remember { CalculatorDatabase.getDatabase(context) }
    val repository = remember { CalculationRepository(database.calculationDao()) }
    val viewModel: CalculatorViewModel = viewModel { CalculatorViewModel(repository) }

    var selectedTab by remember { mutableIntStateOf(0) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "電卓メニュー",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    NavigationDrawerItem(
                        label = { Text("基本") },
                        selected = selectedTab == 0,
                        onClick = { 
                            selectedTab = 0
                            viewModel.setCalculationType(CalculationType.BASIC)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    NavigationDrawerItem(
                        label = { Text("関数") },
                        selected = selectedTab == 1,
                        onClick = { 
                            selectedTab = 1
                            viewModel.setCalculationType(CalculationType.SCIENTIFIC)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    NavigationDrawerItem(
                        label = { Text("進数") },
                        selected = selectedTab == 2,
                        onClick = { 
                            selectedTab = 2
                            viewModel.setCalculationType(CalculationType.PROGRAMMER)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    NavigationDrawerItem(
                        label = { Text("履歴") },
                        selected = selectedTab == 3,
                        onClick = { 
                            selectedTab = 3
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF1C1C1E)) // ダーク背景
                .padding(16.dp)
        ) {
            // ハンバーガーメニューボタン
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
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
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> BasicCalculatorTab(viewModel)
                1 -> ScientificCalculatorTab(viewModel)
                2 -> ProgrammerCalculatorTab(viewModel)
                3 -> HistoryTab(viewModel)
            }
        }
    }
}

@Composable
fun BasicCalculatorTab(viewModel: CalculatorViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 表示部分
        CalculatorDisplay(
            displayText = formatNumberWithCommas(viewModel.displayText.value),
            expression = viewModel.displayExpression,
            previewResult = viewModel.formattedPreviewResult,
            isResultShowing = viewModel.isResultShowing.value,
            modifier = Modifier.weight(0.3f)
        )

        // キーパッド
        BasicKeypad(
            viewModel = viewModel,
            modifier = Modifier.weight(0.7f)
        )
    }
}

@Composable
fun ScientificCalculatorTab(viewModel: CalculatorViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 表示部分
        CalculatorDisplay(
            displayText = formatNumberWithCommas(viewModel.displayText.value),
            expression = viewModel.displayExpression,
            previewResult = viewModel.formattedPreviewResult,
            isResultShowing = viewModel.isResultShowing.value,
            modifier = Modifier.weight(0.3f)
        )

        // 科学計算ボタン
        Column(
            modifier = Modifier.weight(0.3f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButton(
                    text = "sin",
                    onClick = { viewModel.onScientificFunctionClicked("sin") },
                    modifier = Modifier.weight(1f),
                    isOperator = true
                )
                CalculatorButton(
                    text = "cos",
                    onClick = { viewModel.onScientificFunctionClicked("cos") },
                    modifier = Modifier.weight(1f),
                    isOperator = true
                )
                CalculatorButton(
                    text = "tan",
                    onClick = { viewModel.onScientificFunctionClicked("tan") },
                    modifier = Modifier.weight(1f),
                    isOperator = true
                )
                CalculatorButton(
                    text = "√",
                    onClick = { viewModel.onScientificFunctionClicked("sqrt") },
                    modifier = Modifier.weight(1f),
                    isOperator = true
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButton(
                    text = "log",
                    onClick = { viewModel.onScientificFunctionClicked("log") },
                    modifier = Modifier.weight(1f),
                    isOperator = true
                )
                CalculatorButton(
                    text = "ln",
                    onClick = { viewModel.onScientificFunctionClicked("ln") },
                    modifier = Modifier.weight(1f),
                    isOperator = true
                )
                CalculatorButton(
                    text = "π",
                    onClick = { viewModel.onNumberClicked("3.14159") },
                    modifier = Modifier.weight(1f),
                    isOperator = true
                )
                CalculatorButton(
                    text = "e",
                    onClick = { viewModel.onNumberClicked("2.71828") },
                    modifier = Modifier.weight(1f),
                    isOperator = true
                )
            }
        }

        // 基本キーパッド
        BasicKeypad(
            viewModel = viewModel,
            modifier = Modifier.weight(0.4f)
        )
    }
}

@Composable
fun ProgrammerCalculatorTab(viewModel: CalculatorViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 表示部分
        CalculatorDisplay(
            displayText = formatNumberWithCommas(viewModel.displayText.value),
            expression = viewModel.displayExpression,
            previewResult = viewModel.formattedPreviewResult,
            isResultShowing = viewModel.isResultShowing.value,
            modifier = Modifier.weight(0.3f)
        )

        // 進数変換ボタン
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalculatorButton(
                text = "BIN",
                onClick = { viewModel.onBaseConversionClicked("BIN") },
                modifier = Modifier.weight(1f),
                isOperator = true
            )
            CalculatorButton(
                text = "OCT",
                onClick = { viewModel.onBaseConversionClicked("OCT") },
                modifier = Modifier.weight(1f),
                isOperator = true
            )
            CalculatorButton(
                text = "HEX",
                onClick = { viewModel.onBaseConversionClicked("HEX") },
                modifier = Modifier.weight(1f),
                isOperator = true
            )
        }

        // 基本キーパッド
        BasicKeypad(
            viewModel = viewModel,
            modifier = Modifier.weight(0.7f)
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
                        onDelete = { viewModel.deleteHistoryEntry(entry) }
                    )
                }
            }
        }
    }
}

/**
 * 数字を3桁区切りでフォーマットする
 */
private fun formatNumberWithCommas(number: String): String {
    if (number.isEmpty() || number == "Error" || number == "Division by Zero" || number == "Overflow Error") {
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
