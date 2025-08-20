package com.example.test2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.calculator.UnitConverter
import com.example.test2.data.model.UnitCategory
import com.example.test2.data.model.UnitType
import com.example.test2.ui.components.UnitConversionKeypad
import com.example.test2.ui.viewmodel.CalculatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConversionScreen(
    isDarkTheme: Boolean = true,
    viewModel: CalculatorViewModel? = null,
    modifier: Modifier = Modifier,
    onRequestOpenBasic: (() -> Unit)? = null
) {
    val unitConverter = remember { UnitConverter() }
    var selectedCategory by remember { mutableStateOf(UnitCategory.LENGTH) }
    var fromUnit by remember { mutableStateOf<UnitType?>(null) }
    var toUnit by remember { mutableStateOf<UnitType?>(null) }
    var toValue by remember { mutableStateOf("") }
    var showCategorySheet by remember { mutableStateOf(false) }
    var showFromUnitSheet by remember { mutableStateOf(false) }
    var showToUnitSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    val currentUnits = unitConverter.getUnitsForCategory(selectedCategory)
    
    LaunchedEffect(selectedCategory) {
        val units = unitConverter.getUnitsForCategory(selectedCategory)
        if (units.isNotEmpty()) {
            fromUnit = units[0]
            toUnit = if (units.size > 1) units[1] else units[0]
        }
    }
    
    // 電卓の表示値を取得して変換
    val calculatorValue = viewModel?.displayText?.value ?: "0"
    val convertedValue = remember(calculatorValue, fromUnit, toUnit) {
        if (fromUnit != null && toUnit != null) {
            try {
                val input = calculatorValue.replace(",", "").toDoubleOrNull() ?: 0.0
                val result = unitConverter.convert(input, fromUnit!!, toUnit!!)
                unitConverter.formatResult(result)
            } catch (e: Exception) {
                "エラー"
            }
        } else {
            "0"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkTheme) Color(0xFF1C1C1E) else Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
    // 画面タイトルは非表示（カテゴリの上に文字は出さない）

        // カテゴリ選択
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) Color(0xFF2C2C2E) else Color(0xFFF2F2F7)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "カテゴリ",
                    color = if (isDarkTheme) Color.White else Color.Black,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = unitConverter.getCategoryDisplayName(selectedCategory),
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                            unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black
                        )
                    )
                    // 透明オーバーレイでクリック検知
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showCategorySheet = true }
                    )
                }
            }
        }
        
        // 変換元（電卓の値を表示）
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) Color(0xFF2C2C2E) else Color(0xFFF2F2F7)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "変換元（電卓の値）",
                    color = if (isDarkTheme) Color.White else Color.Black,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkTheme) Color(0xFF1C1C1E) else Color(0xFFE5E5EA)
                        )
                    ) {
                        Text(
                            text = calculatorValue,
                            color = if (isDarkTheme) Color.White else Color.Black,
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        )
                    }
                    
                    Box(modifier = Modifier.width(120.dp)) {
                        OutlinedTextField(
                            value = fromUnit?.symbol ?: "",
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                                unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black
                            )
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showFromUnitSheet = true }
                        )
                    }
                }
            }
        }
        
    // 中央のスワップボタンは削除（キーパッド右下の 1↕ で入れ替え可能）
        
        // 変換結果
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) Color(0xFF6750A4).copy(alpha = 0.2f) else Color(0xFF6750A4).copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "変換結果",
                    color = if (isDarkTheme) Color.White else Color.Black,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkTheme) Color(0xFF1C1C1E) else Color(0xFFE5E5EA)
                        )
                    ) {
                        Text(
                            text = convertedValue,
                            color = if (isDarkTheme) Color.White else Color.Black,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        )
                    }
                    
                    Box(modifier = Modifier.width(120.dp)) {
                        OutlinedTextField(
                            value = toUnit?.symbol ?: "",
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                                unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black
                            )
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showToUnitSheet = true }
                        )
                    }
                }
            }
        }

        // Bottom sheets
        if (showCategorySheet) {
            ModalBottomSheet(
                onDismissRequest = { showCategorySheet = false },
                sheetState = sheetState
            ) {
                unitConverter.getAllCategories().forEach { category ->
                    ListItem(
                        headlineContent = { Text(unitConverter.getCategoryDisplayName(category)) },
                        modifier = Modifier.clickable {
                            selectedCategory = category
                            showCategorySheet = false
                        }
                    )
                }
            }
        }
        if (showFromUnitSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFromUnitSheet = false },
                sheetState = sheetState
            ) {
                currentUnits.forEach { unit ->
                    ListItem(
                        headlineContent = { Text("${unit.displayName} (${unit.symbol})") },
                        modifier = Modifier.clickable {
                            fromUnit = unit
                            showFromUnitSheet = false
                        }
                    )
                }
            }
        }
        if (showToUnitSheet) {
            ModalBottomSheet(
                onDismissRequest = { showToUnitSheet = false },
                sheetState = sheetState
            ) {
                currentUnits.forEach { unit ->
                    ListItem(
                        headlineContent = { Text("${unit.displayName} (${unit.symbol})") },
                        modifier = Modifier.clickable {
                            toUnit = unit
                            showToUnitSheet = false
                        }
                    )
                }
            }
        }
        
        // 電卓（単位換算用専用キーパッド）
        viewModel?.let { vm ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                UnitConversionKeypad(
                    viewModel = vm,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    onRequestOpenBasic = onRequestOpenBasic,
                    onSwapUnits = {
                        val tmp = fromUnit
                        fromUnit = toUnit
                        toUnit = tmp
                    }
                )
            }
        }
    }
}