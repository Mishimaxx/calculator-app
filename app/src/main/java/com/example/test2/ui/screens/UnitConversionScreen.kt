package com.example.test2.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.calculator.UnitConverter
import com.example.test2.data.model.UnitCategory
import com.example.test2.data.model.UnitType
import com.example.test2.ui.components.UnitConversionKeypad
import com.example.test2.ui.viewmodel.CalculatorViewModel
import com.example.test2.ui.components.BasicKeypad
import com.example.test2.ui.components.CalculatorDisplay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConversionScreen(
    isDarkTheme: Boolean = true,
    viewModel: CalculatorViewModel? = null,
    onRequestOpenBasic: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // State
    var selectedCategory by remember { mutableStateOf(UnitCategory.LENGTH) }
    var fromUnit by remember { mutableStateOf<UnitType?>(null) }
    var toUnit by remember { mutableStateOf<UnitType?>(null) }

    var showCategorySheet by remember { mutableStateOf(false) }
    var showFromUnitSheet by remember { mutableStateOf(false) }
    var showToUnitSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 一般電卓用のボトムシート状態
    var showBasicSheet by remember { mutableStateOf(false) }
    val basicSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val unitConverter = remember { UnitConverter() }
    val currentUnits = unitConverter.getUnitsForCategory(selectedCategory)

    LaunchedEffect(selectedCategory) {
        val units = unitConverter.getUnitsForCategory(selectedCategory)
        if (units.isNotEmpty()) {
            fromUnit = units.first()
            toUnit = units.getOrNull(1) ?: units.first()
        }
    }

    // Values
    val calculatorValueRaw = viewModel?.formattedDisplayText ?: (viewModel?.displayText?.value ?: "0")
    val convertedValue = remember(calculatorValueRaw, fromUnit, toUnit) {
        if (fromUnit != null && toUnit != null) {
            try {
                val input = calculatorValueRaw.replace(",", "").toDoubleOrNull() ?: 0.0
                val result = unitConverter.convert(input, fromUnit!!, toUnit!!)
                unitConverter.formatResult(result)
            } catch (_: Exception) {
                "エラー"
            }
        } else "0"
    }

    // 変換元/変換結果の長さで可読サイズに調整
    val sourceClean = calculatorValueRaw.replace(",", "")
    val cleanConverted = convertedValue.replace(",", "")
    val sourceTextStyle = when {
        sourceClean.length <= 12 -> MaterialTheme.typography.titleLarge
        sourceClean.length <= 18 -> MaterialTheme.typography.titleMedium
        else -> MaterialTheme.typography.titleSmall
    }
    val resultTextStyle = when {
        cleanConverted.length <= 12 -> MaterialTheme.typography.titleLarge
        cleanConverted.length <= 18 -> MaterialTheme.typography.titleMedium
        else -> MaterialTheme.typography.titleSmall
    }

    val accent = if (isDarkTheme) Color(0xFFFF6B6B) else Color(0xFFDC2626)
    val borderGray = if (isDarkTheme) Color(0xFF30363D) else Color(0xFFD1D9E0)
    val subtleText = if (isDarkTheme) Color(0xFF9AA4AE) else Color(0xFF6B7280)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkTheme) Color(0xFF1C1C1E) else Color.White)
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .padding(bottom = 300.dp) // 下部固定キーパッドとの重なり回避の余白
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
        // カテゴリ（元の配色に復帰）
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) Color(0xFF1C1C1E) else Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isDarkTheme) 0.dp else 2.dp
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "カテゴリ",
                    color = accent,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = unitConverter.getCategoryDisplayName(selectedCategory),
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null, tint = accent) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accent,
                            unfocusedBorderColor = borderGray
                        )
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { showCategorySheet = true })
                }
            }
        }

        // 変換元（電卓の値）
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) Color(0xFF1C1C1E) else Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isDarkTheme) 0.dp else 2.dp
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "変換元（電卓の値）",
                    color = accent,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkTheme) Color(0xFF21262D) else Color(0xFFF6F8FA)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, borderGray)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = calculatorValueRaw,
                                color = if (isDarkTheme) Color(0xFFC9D1D9) else Color(0xFF24292F),
                                style = sourceTextStyle.copy(
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 30.sp
                                ),
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = TextOverflow.Clip,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Box(modifier = Modifier.width(120.dp)) {
                        val fromText = fromUnit?.symbol ?: ""
                        val fromFontSize = when {
                            fromText.length >= 12 -> 9.sp
                            fromText.length >= 9 -> 10.sp
                            fromText.length >= 6 -> 11.sp
                            else -> 13.sp
                        }
                        OutlinedTextField(
                            value = fromText,
                            onValueChange = { },
                            readOnly = true,
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = fromFontSize),
                            trailingIcon = { Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null, tint = accent) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = if (isDarkTheme) Color(0xFFC9D1D9) else Color(0xFF24292F),
                                unfocusedTextColor = if (isDarkTheme) Color(0xFFC9D1D9) else Color(0xFF24292F),
                                focusedBorderColor = accent,
                                unfocusedBorderColor = borderGray
                            )
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { showFromUnitSheet = true })
                    }
                }
            }
        }

    // （移動）単位レート表示は「変換結果」カード右下へ

        // 変換結果（元の赤系配色に復帰）
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) Color(0xFF2D1B1B) else Color(0xFFFEE2E2)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isDarkTheme) 0.dp else 2.dp
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, if (isDarkTheme) accent.copy(alpha = 0.3f) else accent.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "変換結果",
                    color = accent,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkTheme) Color(0xFF21262D) else Color(0xFFFEF2F2)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (isDarkTheme) accent.copy(alpha = 0.4f) else accent.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = convertedValue,
                                color = accent,
                                style = resultTextStyle.copy(fontWeight = FontWeight.Bold, lineHeight = 32.sp),
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Box(modifier = Modifier.width(120.dp)) {
                        val toText = toUnit?.symbol ?: ""
                        val toFontSize = when {
                            toText.length >= 12 -> 9.sp
                            toText.length >= 9 -> 10.sp
                            toText.length >= 6 -> 11.sp
                            else -> 13.sp
                        }
                        OutlinedTextField(
                            value = toText,
                            onValueChange = { },
                            readOnly = true,
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = toFontSize),
                            trailingIcon = { Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null, tint = accent) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = if (isDarkTheme) Color(0xFFC9D1D9) else Color(0xFF24292F),
                                unfocusedTextColor = if (isDarkTheme) Color(0xFFC9D1D9) else Color(0xFF24292F),
                                focusedBorderColor = accent,
                                unfocusedBorderColor = borderGray
                            )
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { showToUnitSheet = true })
                    }
                }

            }
        }

        // レート表示を赤いカードの外（直下・右寄せ）に配置
        if (fromUnit != null && toUnit != null) {
            val unitRateText = remember(fromUnit, toUnit) {
                try {
                    val rate = unitConverter.convert(1.0, fromUnit!!, toUnit!!)
                    val formatted = unitConverter.formatResult(rate)
                    "1${fromUnit!!.displayName} = ${formatted}${toUnit!!.displayName}"
                } catch (_: Exception) {
                    null
                }
            }
            unitRateText?.let { txt ->
                Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    Text(
                        text = txt,
                        color = subtleText,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }
        }

        // Bottom sheets（選択UI）
        if (showCategorySheet) {
            CategoryPickerSheet(
                isDarkTheme = isDarkTheme,
                sheetState = sheetState,
                unitConverter = unitConverter,
                selected = selectedCategory,
                onSelect = { selectedCategory = it },
                onDismiss = { showCategorySheet = false }
            )
        }
        if (showFromUnitSheet) {
            UnitPickerSheet(
                title = "変換元の単位を選択",
                isDarkTheme = isDarkTheme,
                sheetState = sheetState,
                units = currentUnits,
                selected = fromUnit,
                onSelect = { selected ->
                    val prevFrom = fromUnit
                    if (selected.id == toUnit?.id) {
                        // 選択が相手側と同じなら反転
                        fromUnit = selected
                        toUnit = prevFrom
                    } else {
                        fromUnit = selected
                    }
                },
                onDismiss = { showFromUnitSheet = false }
            )
        }
        if (showToUnitSheet) {
            UnitPickerSheet(
                title = "変換先の単位を選択",
                isDarkTheme = isDarkTheme,
                sheetState = sheetState,
                units = currentUnits,
                selected = toUnit,
                onSelect = { selected ->
                    val prevTo = toUnit
                    if (selected.id == fromUnit?.id) {
                        // 選択が相手側と同じなら反転
                        toUnit = selected
                        fromUnit = prevTo
                    } else {
                        toUnit = selected
                    }
                },
                onDismiss = { showToUnitSheet = false }
            )
        }

        // （スクロール領域終わり）
        }

        // 下部固定キーパッド
        viewModel?.let { vm ->
            Surface(
                tonalElevation = 0.dp,
                shadowElevation = 4.dp,
                color = if (isDarkTheme) Color(0xFF1C1C1E) else Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Column(Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp, bottom = 4.dp)) {
                    UnitConversionKeypad(
                        viewModel = vm,
                        modifier = Modifier.fillMaxWidth(),
                        onRequestOpenBasic = { showBasicSheet = true },
                        onSwapUnits = {
                            val tmp = fromUnit
                            fromUnit = toUnit
                            toUnit = tmp
                        }
                    )
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }

        // 開いたら必ずExpandedに
        LaunchedEffect(showBasicSheet) {
            if (showBasicSheet) {
                basicSheetState.expand()
            }
        }

        if (showBasicSheet && viewModel != null) {
            ModalBottomSheet(
                onDismissRequest = { showBasicSheet = false },
                sheetState = basicSheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.9f)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(0.35f).fillMaxWidth()) {
                        CalculatorDisplay(
                            displayText = viewModel.formattedDisplayText,
                            expression = viewModel.displayExpression,
                            expressionField = viewModel.expressionField.value,
                            onExpressionEdited = { viewModel.onExpressionEdited(it) },
                            onArrowKey = { direction -> viewModel.onArrowClicked(direction) },
                            previewResult = viewModel.formattedPreviewResult,
                            isResultShowing = viewModel.isResultShowing.value,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Box(modifier = Modifier.weight(0.65f).fillMaxWidth()) {
                        BasicKeypad(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize(),
                            onEquals = { showBasicSheet = false }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryPickerSheet(
    isDarkTheme: Boolean,
    sheetState: SheetState,
    unitConverter: UnitConverter,
    selected: UnitCategory,
    onSelect: (UnitCategory) -> Unit,
    onDismiss: () -> Unit
) {
    val accent = if (isDarkTheme) Color(0xFFFF6B6B) else Color(0xFFDC2626)
    var query by remember { mutableStateOf("") }
    val categories = remember { UnitCategory.values().toList() }
    val filtered = categories.filter { unitConverter.getCategoryDisplayName(it).contains(query, ignoreCase = true) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            // grabber
            Box(modifier = Modifier.align(Alignment.CenterHorizontally).width(32.dp).height(4.dp).background(Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(2.dp)))
            Spacer(Modifier.height(8.dp))
            // header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("カテゴリを選択", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, contentDescription = null) }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                placeholder = { Text("検索") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { /* no-op */ })
            )
            Spacer(Modifier.height(8.dp))
            Divider()
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(filtered) { cat ->
                    val label = unitConverter.getCategoryDisplayName(cat)
                    ListItem(
                        headlineContent = { Text(label) },
                        trailingContent = {
                            if (cat == selected) Icon(Icons.Filled.Check, contentDescription = null, tint = accent)
                        },
                        modifier = Modifier.clickable {
                            onSelect(cat)
                            onDismiss()
                        }
                    )
                }
            }
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitPickerSheet(
    title: String,
    isDarkTheme: Boolean,
    sheetState: SheetState,
    units: List<UnitType>,
    selected: UnitType?,
    onSelect: (UnitType) -> Unit,
    onDismiss: () -> Unit
) {
    val accent = if (isDarkTheme) Color(0xFFFF6B6B) else Color(0xFFDC2626)
    var query by remember { mutableStateOf("") }
    val filtered = units.filter { u ->
        u.displayName.contains(query, ignoreCase = true) || u.symbol.contains(query, ignoreCase = true)
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            // grabber
            Box(modifier = Modifier.align(Alignment.CenterHorizontally).width(32.dp).height(4.dp).background(Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(2.dp)))
            Spacer(Modifier.height(8.dp))
            // header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, contentDescription = null) }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                placeholder = { Text("検索（記号や名前）") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { /* no-op */ })
            )
            Spacer(Modifier.height(8.dp))
            Divider()
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(filtered) { unit ->
                    ListItem(
                        headlineContent = { Text("${unit.displayName}") },
                        supportingContent = { Text(unit.symbol, color = Color.Gray) },
                        trailingContent = {
                            if (selected?.id == unit.id) Icon(Icons.Filled.Check, contentDescription = null, tint = accent)
                        },
                        modifier = Modifier.clickable {
                            onSelect(unit)
                            onDismiss()
                        }
                    )
                }
            }
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}