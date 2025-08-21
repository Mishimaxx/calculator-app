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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.calculator.CurrencyConverter
import com.example.test2.calculator.CurrencyDefinitions
import com.example.test2.calculator.CurrencyType
import com.example.test2.calculator.CurrencyRateUpdater
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.test2.ui.components.UnitConversionKeypad
import com.example.test2.ui.components.BasicKeypad
import com.example.test2.ui.components.CalculatorDisplay
import com.example.test2.ui.viewmodel.CalculatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConversionScreen(
    isDarkTheme: Boolean = true,
    viewModel: CalculatorViewModel? = null,
    onRequestOpenBasic: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val accent = if (isDarkTheme) Color(0xFFFF6B6B) else Color(0xFFDC2626)
    val borderGray = if (isDarkTheme) Color(0xFF30363D) else Color(0xFFD1D9E0)
    val subtleText = if (isDarkTheme) Color(0xFF9AA4AE) else Color(0xFF6B7280)

    var fromCurrency by remember { mutableStateOf(CurrencyDefinitions.CURRENCIES.first()) }
    var toCurrency by remember { mutableStateOf(CurrencyDefinitions.CURRENCIES.getOrNull(1) ?: CurrencyDefinitions.CURRENCIES.first()) }

    var isUpdating by remember { mutableStateOf(false) }
    var lastUpdated by remember { mutableStateOf<String?>(null) }
    var updateError by remember { mutableStateOf<String?>(null) }
    // レート更新トリガ（内部オブジェクトの ratePerUSD 変更は Compose に察知されないため明示的に再計算キーを用意）
    var rateTick by remember { mutableIntStateOf(0) }

    val scope = rememberCoroutineScope()

    var showFromSheet by remember { mutableStateOf(false) }
    var showToSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 一般電卓シート
    var showBasicSheet by remember { mutableStateOf(false) }
    val basicSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val converter = remember { CurrencyConverter() }

    val rawValue = viewModel?.formattedDisplayText ?: (viewModel?.displayText?.value ?: "0")
    val amount = remember(rawValue) { rawValue.replace(",", "").toDoubleOrNull() ?: 0.0 }
    val converted = remember(amount, fromCurrency, toCurrency, rateTick) {
        try {
            converter.format(converter.convert(amount, fromCurrency, toCurrency))
        } catch (_: Exception) { "エラー" }
    }

    val cleanConverted = converted.replace(",", "")
    val sourceClean = rawValue.replace(",", "")
    val sourceStyle = when {
        sourceClean.length <= 12 -> MaterialTheme.typography.titleLarge
        sourceClean.length <= 18 -> MaterialTheme.typography.titleMedium
        else -> MaterialTheme.typography.titleSmall
    }
    val resultStyle = when {
        cleanConverted.length <= 12 -> MaterialTheme.typography.titleLarge
        cleanConverted.length <= 18 -> MaterialTheme.typography.titleMedium
        else -> MaterialTheme.typography.titleSmall
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkTheme) Color(0xFF1C1C1E) else Color.White)
    ) {
        // スクロール可能コンテンツ（キーパッド上部余白確保）
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .padding(bottom = 300.dp) // キーパッド重なり回避のため十分な余白（概ね4行分）
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
    // （旧）更新行は削除し、下部のレート表示直後に配置

        // 変換元
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) Color(0xFF1C1C1E) else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkTheme) 0.dp else 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("変換元", color = accent, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) Color(0xFF21262D) else Color(0xFFF6F8FA)),
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
                                text = rawValue,
                                color = if (isDarkTheme) Color(0xFFC9D1D9) else Color(0xFF24292F),
                                style = sourceStyle.copy(fontWeight = FontWeight.Medium, lineHeight = 30.sp),
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = TextOverflow.Clip,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Box(Modifier.width(140.dp)) {
                        OutlinedTextField(
                            value = fromCurrency.code,
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            trailingIcon = { Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null, tint = accent) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accent,
                                unfocusedBorderColor = borderGray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(Modifier.matchParentSize().clickable { showFromSheet = true })
                    }
                }
            }
        }

        // 変換結果
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) Color(0xFF2D1B1B) else Color(0xFFFEE2E2)),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkTheme) 0.dp else 2.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, if (isDarkTheme) accent.copy(alpha = 0.3f) else accent.copy(alpha = 0.2f))
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("変換結果", color = accent, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) Color(0xFF21262D) else Color(0xFFFEF2F2)),
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
                                text = converted,
                                color = accent,
                                style = resultStyle.copy(fontWeight = FontWeight.Bold, lineHeight = 32.sp),
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Box(Modifier.width(140.dp)) {
                        OutlinedTextField(
                            value = toCurrency.code,
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            trailingIcon = { Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null, tint = accent) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accent,
                                unfocusedBorderColor = borderGray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(Modifier.matchParentSize().clickable { showToSheet = true })
                    }
                }
            }
        }

        // レート表示
    val rateLine = remember(fromCurrency, toCurrency, rateTick) {
            val rate = converter.convert(1.0, fromCurrency, toCurrency)
            "1 ${fromCurrency.code} = ${converter.format(rate)} ${toCurrency.code}"
        }
        Box(Modifier.fillMaxWidth().padding(top = 4.dp)) {
            Text(
                text = rateLine,
                color = subtleText,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }

        // レート更新カード（スタイリッシュな半透明/グラデーション風）
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkTheme) Color(0xFF262B33) else Color(0xFFF3F6FA)
            ),
            border = BorderStroke(1.dp, if (isDarkTheme) Color(0xFF3A434E) else Color(0xFFD4DEE7))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "レート更新",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = accent
                        )
                        val sub = when {
                            isUpdating -> "最新レート取得中..."
                            lastUpdated != null -> "最終更新: ${lastUpdated}"
                            else -> "最新の為替レートを取得"
                        }
                        Text(
                            text = sub,
                            style = MaterialTheme.typography.bodySmall,
                            color = subtleText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Button(
                        enabled = !isUpdating,
                        onClick = {
                            if (isUpdating) return@Button
                            isUpdating = true
                            updateError = null
                            // 成功時非表示のため旧スニペットクリア
                            CurrencyRateUpdater.lastBodySnippet = null
                            scope.launch {
                                val result = withContext(Dispatchers.IO) { CurrencyRateUpdater.fetchAndUpdate() }
                                if (result == null || result.isEmpty()) {
                                    updateError = CurrencyRateUpdater.lastErrorMessage ?: "取得失敗"
                                } else {
                                    lastUpdated = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                                    // レート値変更を UI に反映させるためトリガ増加
                                    rateTick++
                                }
                                isUpdating = false
                                if (updateError == null) {
                                    // 成功ならスニペットは出さない
                                    CurrencyRateUpdater.lastBodySnippet = null
                                }
                                fromCurrency = CurrencyDefinitions.find(fromCurrency.code) ?: fromCurrency
                                toCurrency = CurrencyDefinitions.find(toCurrency.code) ?: toCurrency
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accent,
                            contentColor = Color.White,
                            disabledContainerColor = accent.copy(alpha = 0.4f),
                            disabledContentColor = Color.White.copy(alpha = 0.7f)
                        )
                    ) {
                        if (isUpdating) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("更新中")
                        } else {
                            Text("更新")
                        }
                    }
                }
                if (updateError != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDarkTheme) Color(0xFF372526) else Color(0xFFFFEBEB))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(updateError ?: "", color = accent, style = MaterialTheme.typography.labelMedium)
                        CurrencyRateUpdater.lastBodySnippet?.let { snip ->
                            Text(
                                text = snip,
                                color = subtleText,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                if (isUpdating) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        trackColor = if (isDarkTheme) Color(0xFF3A434E) else Color(0xFFE0E7EF),
                        color = accent
                    )
                }
            }
        }

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
                            val tmp = fromCurrency
                            fromCurrency = toCurrency
                            toCurrency = tmp
                        }
                    )
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }

        LaunchedEffect(showBasicSheet) { if (showBasicSheet) basicSheetState.expand() }

        if (showBasicSheet && viewModel != null) {
            ModalBottomSheet(onDismissRequest = { showBasicSheet = false }, sheetState = basicSheetState) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.9f)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(Modifier.weight(0.35f).fillMaxWidth()) {
                        CalculatorDisplay(
                            displayText = viewModel.formattedDisplayText,
                            expression = viewModel.displayExpression,
                            expressionField = viewModel.expressionField.value,
                            onExpressionEdited = { viewModel.onExpressionEdited(it) },
                            onArrowKey = { d -> viewModel.onArrowClicked(d) },
                            previewResult = viewModel.formattedPreviewResult,
                            isResultShowing = viewModel.isResultShowing.value,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Box(Modifier.weight(0.65f).fillMaxWidth()) {
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

    // シート: 通貨選択
    if (showFromSheet) {
        CurrencyPickerSheet(
            title = "変換元通貨を選択",
            isDarkTheme = isDarkTheme,
            sheetState = sheetState,
            selected = fromCurrency,
            onSelect = { sel ->
                if (sel.code == toCurrency.code) {
                    // 逆転
                    val prev = fromCurrency
                    fromCurrency = sel
                    toCurrency = prev
                } else {
                    fromCurrency = sel
                }
            },
            onDismiss = { showFromSheet = false }
        )
    }
    if (showToSheet) {
        CurrencyPickerSheet(
            title = "変換先通貨を選択",
            isDarkTheme = isDarkTheme,
            sheetState = sheetState,
            selected = toCurrency,
            onSelect = { sel ->
                if (sel.code == fromCurrency.code) {
                    val prev = toCurrency
                    toCurrency = sel
                    fromCurrency = prev
                } else {
                    toCurrency = sel
                }
            },
            onDismiss = { showToSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyPickerSheet(
    title: String,
    isDarkTheme: Boolean,
    sheetState: SheetState,
    selected: CurrencyType?,
    onSelect: (CurrencyType) -> Unit,
    onDismiss: () -> Unit
) {
    val accent = if (isDarkTheme) Color(0xFFFF6B6B) else Color(0xFFDC2626)
    var query by remember { mutableStateOf("") }
    val list = CurrencyDefinitions.CURRENCIES

    // よく使う通貨（固定）
    // USD を追加
    val frequentCodes = remember { listOf("USD","EUR","GBP","HKD","CNY","JPY","AUD","CAD","INR","KRW") }
    val frequent = remember(list) { list.filter { it.code in frequentCodes } }

    // 最近使った通貨（画面存続中のみ保持）
    // Composition ローカルで共有するため rememberSaveable ではなく remember (セッション内)
    val recentStore = remember { mutableStateListOf<CurrencyType>() }

    fun pushRecent(cur: CurrencyType) {
        val existingIndex = recentStore.indexOfFirst { it.code == cur.code }
        if (existingIndex >= 0) recentStore.removeAt(existingIndex)
        recentStore.add(0, cur)
        // 上限 8 件 (API24互換: removeLast() ではなく removeAt(lastIndex))
        while (recentStore.size > 8) recentStore.removeAt(recentStore.lastIndex)
    }

    val filtered = list.filter { it.code.contains(query, true) || it.displayName.contains(query, true) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Box(Modifier.align(Alignment.CenterHorizontally).width(32.dp).height(4.dp).background(Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(2.dp)))
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, contentDescription = null) }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                placeholder = { Text("検索 (コード/名称)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { /*no-op*/ })
            )
            Spacer(Modifier.height(8.dp))
            Divider()
            LazyColumn(Modifier.fillMaxWidth()) {
                if (query.isEmpty()) {
                    if (recentStore.isNotEmpty()) {
                        item {
                            Text("最近", style = MaterialTheme.typography.labelLarge, color = accent, modifier = Modifier.padding(top = 4.dp, bottom = 4.dp))
                        }
                        items(recentStore) { cur ->
                            ListItem(
                                headlineContent = { Text("${cur.code} - ${cur.displayName}") },
                                trailingContent = {
                                    if (selected?.code == cur.code) Icon(Icons.Filled.Check, contentDescription = null, tint = accent)
                                },
                                modifier = Modifier.clickable {
                                    pushRecent(cur)
                                    onSelect(cur)
                                    onDismiss()
                                }
                            )
                        }
                        item { Divider(modifier = Modifier.padding(vertical = 4.dp)) }
                    }
                    item {
                        Text("よく使われる通貨", style = MaterialTheme.typography.labelLarge, color = accent, modifier = Modifier.padding(top = 4.dp, bottom = 4.dp))
                    }
                    items(frequent) { cur ->
                        ListItem(
                            headlineContent = { Text("${cur.code} - ${cur.displayName}") },
                            trailingContent = {
                                if (selected?.code == cur.code) Icon(Icons.Filled.Check, contentDescription = null, tint = accent)
                            },
                            modifier = Modifier.clickable {
                                pushRecent(cur)
                                onSelect(cur)
                                onDismiss()
                            }
                        )
                    }
                    item { Divider(modifier = Modifier.padding(vertical = 4.dp)) }
                }
                // 通常/検索結果
                items(filtered) { cur ->
                    ListItem(
                        headlineContent = { Text("${cur.code} - ${cur.displayName}") },
                        trailingContent = {
                            if (selected?.code == cur.code) Icon(Icons.Filled.Check, contentDescription = null, tint = accent)
                        },
                        modifier = Modifier.clickable {
                            pushRecent(cur)
                            onSelect(cur)
                            onDismiss()
                        }
                    )
                }
            }
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}
