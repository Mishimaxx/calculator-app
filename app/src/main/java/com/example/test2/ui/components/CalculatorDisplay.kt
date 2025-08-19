package com.example.test2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.remember
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.flow.emptyFlow
import android.view.inputmethod.InputMethodManager
import android.content.Context
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min

/**
 * 電卓の表示画面コンポーネント（CASIO風液晶ディスプレイ）
 */
@Composable
fun CalculatorDisplay(
    displayText: String,
    expression: String,
    expressionField: TextFieldValue? = null,
    onExpressionEdited: ((TextFieldValue) -> Unit)? = null,
    onArrowKey: ((String) -> Unit)? = null,
    previewResult: String = "",
    isResultShowing: Boolean = false,
    modifier: Modifier = Modifier
) {
    // 文字数に応じて動的にフォントサイズを調整する関数
    fun calculateFontSize(text: String, baseFontSize: Float, maxLength: Int = 20): Float {
        return when {
            text.length <= maxLength / 2 -> baseFontSize
            text.length <= maxLength -> baseFontSize * 0.8f
            text.length <= maxLength * 1.5 -> baseFontSize * 0.65f
            else -> baseFontSize * 0.45f // より小さくして長い式に対応
        }
    }

    // 演算子の間隔を調整する関数
    fun formatExpressionSpacing(expression: String): String {
        return expression
            .replace(" + ", "+")
            .replace(" - ", "-") 
            .replace(" × ", "×")
            .replace(" ÷ ", "÷")
            .replace(" * ", "×")
            .replace(" / ", "÷")
            // スペースなしの演算子も変換（EXP由来の *10^ などを対処）
            .replace("*", "×")
            .replace("/", "÷")
            // スペースなしで直接つなげる（より狭い間隔）
            .replace("+", "+")
            .replace("-", "-")
            .replace("×", "×")
            .replace("÷", "÷")
    }

    // 未入力のプレースホルダーが含まれているかチェック（数字なしの場合のみ）
    fun hasUnfilledPlaceholder(text: String): Boolean {
        // 既に□が含まれている場合は、未入力のプレースホルダーとして扱う
        if (text.contains("□")) {
            return true
        }
        
        // 数字が含まれている場合は未入力プレースホルダーではない
        if (text.any { it.isDigit() }) {
            return false
        }
        
        return text.contains("²") || text.contains("³") || text.contains("⁻¹") || text.contains("⁻³") ||
               text.contains("!") || text.contains("sin(") || text.contains("cos(") || text.contains("tan(") ||
               text.contains("log(") || text.contains("ln(") || text.contains("√") || text.contains("³√") ||
               text.contains("10^") || text.contains("e") || text.contains("P") || text.contains("C") ||
               text.endsWith("+") || text.endsWith("-") || text.endsWith("×") || text.endsWith("÷") ||
               text.endsWith("*") || text.endsWith("/") || text.endsWith("%")
    }

    // 必要に応じてプレースホルダーを追加
    fun addPlaceholderIfNeeded(text: String): String {
        // 既にプレースホルダーがある場合はそのまま返す（重複を防ぐ）
        if (text.contains("□")) {
            return text
        }
        
        return when {
            // べき乗記号（数字がない場合のみ）
            text == "²" -> "□²"
            text == "³" -> "□³"
            text == "⁻¹" -> "□⁻¹"
            text == "⁻³" -> "□⁻³"
            
            // 階乗（数字がない場合のみ）: ViewModel側で基数チェック済みのため、ここでは変換しない
            text == "!" -> "!"
            
            // 三角関数（開き括弧のみの場合）
            text == "sin(" -> "sin(□"
            text == "cos(" -> "cos(□"
            text == "tan(" -> "tan(□"
            text == "asin(" -> "asin(□"
            text == "acos(" -> "acos(□"
            text == "atan(" -> "atan(□"
            
            // 対数関数（開き括弧のみの場合）
            text == "log(" -> "log(□"
            text == "ln(" -> "ln(□"
            
            // 平方根・立方根（記号のみの場合）
            text == "√" -> "√□"
            text == "³√" -> "³√□"
            
            // 指数（記号のみの場合）
            text == "10^" -> "10^□"
            text == "e" -> "e□"
            
            // 順列・組み合わせ（記号のみの場合）
            text == "P" -> "□P□"
            text == "C" -> "□C□"
            
            // 演算子で終わる場合
            text.endsWith("+") && text.length == 1 -> "□+"
            text.endsWith("-") && text.length == 1 -> "□-"
            text.endsWith("×") && text.length == 1 -> "□×"
            text.endsWith("÷") && text.length == 1 -> "□÷"
            text.endsWith("*") && text.length == 1 -> "□*"
            text.endsWith("/") && text.length == 1 -> "□/"
            text.endsWith("%") && text.length == 1 -> "□%"
            
            else -> text
        }
    }

    // ダークなグレーの背景
    val displayBackground = Color(0xFF404040) // ダークなグレーの背景
    val textColor = Color.White // 白い文字色
    val resultColor = Color(0xFFFF6B6B) // ボタンと同じ赤色

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(displayBackground)
            .padding(16.dp)
    ) {
        // 上付き文字（指数）用の共通スタイル
        val SuperscriptStyle = SpanStyle(
            // 少し小さくして上に上げると“右下に下がる”印象が解消される
            fontSize = 0.78.em,
            baselineShift = BaselineShift(0.82f),
            letterSpacing = (-0.03).em,
        )
        // メインディスプレイエリア - 下の方に配置
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom
        ) {
            // 上側は空白スペース
            Spacer(modifier = Modifier.weight(1f))

            // 式表示部分
            val exprText = expression
            val expressionFontSize = calculateFontSize(exprText, if (isResultShowing) 18f else 32f, 30)
            if (onExpressionEdited != null && expressionField != null) {
                // 直接編集可能（キャレット/選択有効）
                // 視覚変換: 入力テキストの * と / を × と ÷ に置換（テキスト値は保持）
                val operatorVisualTransformation = remember {
                    VisualTransformation { text ->
                        val raw = text.text
                        // 文字列を走査しながら視覚文字列を構築し、同時にオフセット対応セグメントを記録
                        data class Seg(val oStart: Int, val oEnd: Int, val tStart: Int, val tEnd: Int)
                        val segs = mutableListOf<Seg>()
                        val sb = StringBuilder()
                        // 上付きスタイル適用範囲（生成文字列側のインデックス）
                        val supRanges = mutableListOf<Pair<Int, Int>>()
                        var i = 0
                        fun emit(consumed: Int, producedStr: String, sup: Boolean = false) {
                            val tStart = sb.length
                            sb.append(producedStr)
                            val tEnd = sb.length
                            segs += Seg(i, i + consumed, tStart, tEnd)
                            if (sup) supRanges += (tStart to tEnd)
                            i += consumed
                        }
                        fun hasBaseChar(idxBeforeCaret: Int): Boolean {
                            if (idxBeforeCaret < 0) return false
                            val pc = raw[idxBeforeCaret]
                            return pc.isDigit() || pc == ')' || pc == 'π' || pc == 'e' || "⁰¹²³⁴⁵⁶⁷⁸⁹".contains(pc)
                        }
                        while (i < raw.length) {
                            // 先に三文字の演算子表現 " <op> " をスペース無しに潰す
                            if (i + 3 <= raw.length) {
                                val tri = raw.substring(i, i + 3)
                                when (tri) {
                                    " + " -> { emit(3, "+"); continue }
                                    " - " -> { emit(3, "-"); continue }
                                    " * " -> { emit(3, "×"); continue }
                                    " / " -> { emit(3, "÷"); continue }
                                }
                            }
                            // EXPの *10^ は '^' をここで消費しない："×10"を出し、次の '^' で□/上付きへ
                            if (i + 4 <= raw.length && raw.substring(i, i + 4) == "*10^") {
                                // 消費は3（"*10"）、次ループで '^' を一般の^ハンドラに任せる
                                emit(3, "×10")
                                continue
                            }
                            // 同長置換: * と / を × ÷ へ
                            val c = raw[i]
                            if (c == '*') { emit(1, "×"); continue }
                            if (c == '/') { emit(1, "÷"); continue }
                            // 通常の数値トークンに3桁区切りを適用（指数は別処理済みなのでここには来ない）
                            if (c.isDigit()) {
                                var j = i
                                // 整数部
                                while (j < raw.length && raw[j].isDigit()) j++
                                // 小数点・小数部（途中の"."だけでも維持）
                                if (j < raw.length && raw[j] == '.') {
                                    j++
                                    while (j < raw.length && raw[j].isDigit()) j++
                                }
                                val token = raw.substring(i, j)
                                fun groupIntPart(num: String): String {
                                    val dotIdx = num.indexOf('.')
                                    val intPart = if (dotIdx >= 0) num.substring(0, dotIdx) else num
                                    val decPart = if (dotIdx >= 0) num.substring(dotIdx) else ""
                                    val groupedInt = intPart.reversed().chunked(3).joinToString(",").reversed()
                                    return groupedInt + decPart
                                }
                                val produced = groupIntPart(token)
                                emit(j - i, produced)
                                continue
                            }
                            // 上付き視覚変換（長さが変わる）
                            if (i + 3 <= raw.length && raw.substring(i, i + 3) == "^-1") {
                                val withBase = hasBaseChar(i - 1)
                                emit(3, if (withBase) "⁻¹" else "□⁻¹", sup = true)
                                continue
                            }
                            if (i + 3 <= raw.length && raw.substring(i, i + 3) == "^-3") {
                                val withBase = hasBaseChar(i - 1)
                                emit(3, if (withBase) "⁻³" else "□⁻³", sup = true)
                                continue
                            }
                            if (i + 2 <= raw.length && raw.substring(i, i + 2) == "^2") {
                                val withBase = hasBaseChar(i - 1)
                                emit(2, if (withBase) "²" else "□²", sup = true)
                                continue
                            }
                            if (i + 2 <= raw.length && raw.substring(i, i + 2) == "^3") {
                                val withBase = hasBaseChar(i - 1)
                                emit(2, if (withBase) "³" else "□³", sup = true)
                                continue
                            }
                            // 一般の ^<符号付き数字列> を検出し、^ を隠して以降を上付き表示
                            if (raw[i] == '^') {
                                val withBase = hasBaseChar(i - 1)
                                var j = i + 1
                                // 先頭だけ負号許可
                                if (j < raw.length && raw[j] == '-') j++
                                var consumed = 0
                                while (j + consumed < raw.length && raw[j + consumed].isDigit()) consumed++
                                if (consumed > 0) {
                                    val hasMinus = (i + 1 < raw.length && raw[i + 1] == '-')
                                    val segEnd = (i + 1) + (if (hasMinus) 1 else 0) + consumed
                                    val seg = raw.substring(i + 1, segEnd)
                                    val sup = buildString {
                                        for (c in seg) {
                                            val mapped = when (c) {
                                                '0' -> '⁰'
                                                '1' -> '¹'
                                                '2' -> '²'
                                                '3' -> '³'
                                                '4' -> '⁴'
                                                '5' -> '⁵'
                                                '6' -> '⁶'
                                                '7' -> '⁷'
                                                '8' -> '⁸'
                                                '9' -> '⁹'
                                                '-' -> '⁻'
                                                else -> c
                                            }
                                            append(mapped)
                                        }
                                    }
                                    val consumedTotal = 1 + (if (hasMinus) 1 else 0) + consumed
                                    emit(consumedTotal, if (withBase) sup else "□$sup", sup = true)
                                    continue
                                } else {
                                    // まだ指数が未入力なら、上付きプレースホルダのみ表示
                                    emit(1, "□", sup = true)
                                    continue
                                }
                            }
                            // それ以外は1文字そのまま
                            emit(1, c.toString())
                        }
                        val vis = sb.toString()
                        val annotated = buildAnnotatedString {
                            append(vis)
                            // 上付き領域にスタイルを適用
                            supRanges.forEach { (s, e) ->
                                addStyle(SuperscriptStyle, s, e)
                            }
                        }
                        // オフセットマッピング: セグメント列に基づき相互変換
                        val mapping = object : OffsetMapping {
                            override fun originalToTransformed(offset: Int): Int {
                                if (segs.isEmpty()) return offset
                                var accO = 0
                                var accT = 0
                                for (s in segs) {
                                    val oLen = s.oEnd - s.oStart
                                    val tLen = s.tEnd - s.tStart
                                    if (offset <= s.oStart) return accT + (offset - accO)
                                    if (offset <= s.oEnd) {
                                        return if (oLen == tLen) {
                                            s.tStart + (offset - s.oStart)
                                        } else {
                                            // 置換中は末尾にスナップ
                                            s.tEnd
                                        }
                                    }
                                    accO = s.oEnd
                                    accT = s.tEnd
                                }
                                // 最後を超える場合は末尾へ
                                return accT + (offset - accO)
                            }
                            override fun transformedToOriginal(offset: Int): Int {
                                if (segs.isEmpty()) return offset
                                var accO = 0
                                var accT = 0
                                for (s in segs) {
                                    val oLen = s.oEnd - s.oStart
                                    val tLen = s.tEnd - s.tStart
                                    if (offset <= s.tStart) return accO + (offset - accT)
                                    if (offset <= s.tEnd) {
                                        return if (oLen == tLen) {
                                            s.oStart + (offset - s.tStart)
                                        } else {
                                            // 置換中は元の末尾にスナップ
                                            s.oEnd
                                        }
                                    }
                                    accO = s.oEnd
                                    accT = s.tEnd
                                }
                                return accO + (offset - accT)
                            }
                        }
                        TransformedText(annotated, mapping)
                    }
                }
                val focusRequester = remember { FocusRequester() }
                val context = LocalContext.current
                val view = LocalView.current
                val keyboardController = LocalSoftwareKeyboardController.current
                
                
                LaunchedEffect(Unit) {
                    // 複数段階でキーボードを隠す
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    
                    // 段階1: 全ての方法でキーボードを隠す
                    keyboardController?.hide()
                    imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
                    imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                    
                    kotlinx.coroutines.delay(100)
                    
                    // 段階2: フォーカス取得
                    focusRequester.requestFocus()
                    
                    // 段階3: フォーカス直後に即座に再度隠す
                    kotlinx.coroutines.delay(1)
                    keyboardController?.hide()
                    imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
                    imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                    
                    // 段階4: 念押しでもう一度
                    kotlinx.coroutines.delay(50)
                    keyboardController?.hide()
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }
                
                BasicTextField(
                    value = expressionField,
                    onValueChange = { newValue ->
                        // キーボードが出ようとしたら即座に隠す - 複数の方法で確実に
                        keyboardController?.hide()
                        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
                        imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                        imm.hideSoftInputFromWindow(view.windowToken, 0)
                        onExpressionEdited?.invoke(newValue)
                    },
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = expressionFontSize.sp,
                        color = textColor,
                        textAlign = TextAlign.End,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isResultShowing) FontWeight.Normal else FontWeight.Bold,
                        lineHeight = (expressionFontSize * 0.9f).sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            // フォーカス時に即座にキーボードを隠す - あらゆる方法で
                            keyboardController?.hide()
                            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
                            imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                            imm.hideSoftInputFromWindow(view.windowToken, 0)
                        }
                        .pointerInput(Unit) {
                            // タッチイベントを検出してもキーボードを隠す
                            detectTapGestures {
                                keyboardController?.hide()
                                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                imm.hideSoftInputFromWindow(view.windowToken, 0)
                            }
                        },
                    readOnly = false,
                    singleLine = false,
                    maxLines = 2,
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(textColor),
                    visualTransformation = operatorVisualTransformation,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Unspecified,
                        imeAction = ImeAction.None,
                        autoCorrect = false
                    ),
                    keyboardActions = KeyboardActions(
                        onAny = {
                            keyboardController?.hide()
                            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(view.windowToken, 0)
                        }
                    ),
                    interactionSource = remember { 
                        object : MutableInteractionSource {
                            override val interactions = emptyFlow<androidx.compose.foundation.interaction.Interaction>()
                            override suspend fun emit(interaction: androidx.compose.foundation.interaction.Interaction) {
                                // キーボードを隠す
                                keyboardController?.hide()
                            }
                            override fun tryEmit(interaction: androidx.compose.foundation.interaction.Interaction): Boolean {
                                // キーボードを隠す
                                keyboardController?.hide()
                                return true
                            }
                        }
                    }
                )
            } else if (exprText.isNotEmpty()) {
                val formattedExpression = formatExpressionSpacing(exprText)
                val annotatedExpression = buildAnnotatedSuperscripts(formattedExpression, SuperscriptStyle)
                Text(
                    text = annotatedExpression,
                    fontSize = expressionFontSize.sp,
                    color = textColor,
                    textAlign = TextAlign.End,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (isResultShowing) FontWeight.Normal else FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    lineHeight = (expressionFontSize * 0.9f).sp
                )
            }

            // 結果表示
            if (previewResult.isNotEmpty() || isResultShowing || displayText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val resultText = if (isResultShowing) displayText else if (previewResult.isNotEmpty()) previewResult else displayText
                // = 押したら結果を大きく、式入力時は小さく
                val resultFontSize = calculateFontSize(resultText, if (isResultShowing) 40f else 20f, 20)
                
                        val displayLineRaw = if (!isResultShowing && previewResult.isNotEmpty()) {
                            if (resultText.startsWith("=")) resultText else "= $resultText"
                        } else if (!isResultShowing && hasUnfilledPlaceholder(resultText)) {
                            val dt = addPlaceholderIfNeeded(resultText)
                            "= $dt"
                        } else if (isResultShowing) {
                            formatNumberWithCommasSafe(resultText)
                        } else resultText

                        // EXP起因の '*10^' を表示用の '×10' に変換（'^'は注釈ビルダーで処理）
                        val displayLine = formatExpressionSpacing(displayLineRaw)
                            .replace("*10^", "×10^")
                            .replace("*", "×")
                            .replace("/", "÷")
                            // EXP変換後に稀に重複する×を一本化
                            .replace("××10", "×10")

                        if (isResultShowing) {
                            // 赤い確定結果は1行固定。はみ出す場合は自動で小さくする
                            val annotated = buildAnnotatedSuperscripts(displayLine, SuperscriptStyle)
                            AutoResizeText(
                                text = annotated,
                                color = resultColor,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                baseFontSizeSp = resultFontSize,
                                minFontSizeSp = 12f,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            val annotatedPreview = buildAnnotatedSuperscripts(displayLine, SuperscriptStyle)
                            Text(
                                text = annotatedPreview,
                                fontSize = resultFontSize.sp,
                                color = textColor,
                                textAlign = TextAlign.End,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.fillMaxWidth(),
                                lineHeight = (resultFontSize * 0.9f).sp
                            )
                        }
            }
        }
    }
}

        @Composable
        private fun AutoResizeText(
            text: AnnotatedString,
            color: Color,
            fontFamily: FontFamily,
            fontWeight: FontWeight,
            baseFontSizeSp: Float,
            minFontSizeSp: Float = 12f,
            modifier: Modifier = Modifier
        ) {
            // テキストがはみ出すならフォントサイズを段階的に小さくして1行に収める
            val sizeState = remember(text, baseFontSizeSp) { mutableStateOf(baseFontSizeSp) }
            val currentSize = sizeState.value

            Text(
                text = text,
                color = color,
                fontSize = currentSize.sp,
                fontFamily = fontFamily,
                fontWeight = fontWeight,
                textAlign = TextAlign.End,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
                modifier = modifier,
                onTextLayout = { layout ->
                    if (layout.hasVisualOverflow && sizeState.value > minFontSizeSp) {
                        sizeState.value = (sizeState.value - 2f).coerceAtLeast(minFontSizeSp)
                    }
                }
            )
        }

        // "a×10^b" または "a×10ⁿ" のような文字列から、指数部をベースライン上げで綺麗に表示する
    private fun buildAnnotatedPowerOfTen(input: String, supStyle: SpanStyle): AnnotatedString? {
            // パターン: 任意のmantissa + ×10 + 上付き指数
            val idx = input.indexOf("×10")
            if (idx <= 0 || idx + 2 >= input.length) return null
            val mantissa = input.substring(0, idx)
            val rest = input.substring(idx + 2) // 先頭は "10…"
            if (!rest.startsWith("10")) return null
            val expPart = rest.removePrefix("10")
            if (expPart.isEmpty()) return null
            return buildAnnotatedString {
                append(mantissa)
                append("×10")
                // 指数を上付きスタイルで（BaselineShift=Superscript）
        withStyle(supStyle) {
                    // 上付き数字は通常数字に戻して描画（位置/高さを統一）
                    expPart.forEach { c ->
                        val mapped = when (c) {
                            '⁰' -> '0'
                            '¹' -> '1'
                            '²' -> '2'
                            '³' -> '3'
                            '⁴' -> '4'
                            '⁵' -> '5'
                            '⁶' -> '6'
                            '⁷' -> '7'
                            '⁸' -> '8'
                            '⁹' -> '9'
                            '⁻' -> '-'
                            else -> c
                        }
                        append(mapped)
                    }
                }
            }
        }

        // 表示用の文字列中に含まれる上付き（⁰¹²…や⁻）の連続を検出し、通常数字へ戻してBaselineShiftで統一表示
    private fun buildAnnotatedSuperscripts(input: String, supStyle: SpanStyle): AnnotatedString {
            val supSet = setOf('⁰','¹','²','³','⁴','⁵','⁶','⁷','⁸','⁹','⁻')
            val expCharSet = setOf('0','1','2','3','4','5','6','7','8','9','-','⁰','¹','²','³','⁴','⁵','⁶','⁷','⁸','⁹','⁻','□')
            return buildAnnotatedString {
                var i = 0
                while (i < input.length) {
                    // 0-1) ×10 の直後に '^' があれば隠して、以降を上付き
                    if (input.startsWith("×10", i)) {
                        append("×10")
                        i += 3
                        if (i < input.length && input[i] == '^') {
                            i += 1 // '^' を隠す
                        }
                        val start = i
                        var j = i
                        while (j < input.length && input[j] in expCharSet) j++
                        val segment = input.substring(start, j)
                        if (segment.isNotEmpty()) {
                            withStyle(supStyle) { append(segment) }
                        }
                        i = j
                        continue
                    }
                    // （上の分岐に統合）
                    // 1) 10^ または e^ を検出し、^ を隠して以降の連続を上付き描画
                    if (input.startsWith("10^", i)) {
                        append("10")
                        i += 3 // skip "10^"
                        val start = i
                        var j = i
                        while (j < input.length && input[j] in expCharSet) j++
                        val segment = input.substring(start, j)
                        withStyle(supStyle) { append(segment) }
                        i = j
                        continue
                    }
                    if (input.startsWith("e^", i)) {
                        append("e")
                        i += 2 // skip "e^"
                        val start = i
                        var j = i
                        while (j < input.length && input[j] in expCharSet) j++
                        val segment = input.substring(start, j)
                        withStyle(supStyle) { append(segment) }
                        i = j
                        continue
                    }
                    
                    // 1.5) 一般べき乗の^を検出し、隠して以降を上付き描画
                    if (input[i] == '^') {
                        i += 1 // skip "^"
                        val start = i
                        var j = i
                        while (j < input.length && input[j] in expCharSet) j++
                        val segment = input.substring(start, j)
                        if (segment.isNotEmpty()) {
                            withStyle(supStyle) { append(segment) }
                        }
                        i = j
                        continue
                    }

                    // 1.6) ベース直後の『□』は指数のプレースホルダーとして上付き描画
                    if (input[i] == '□') {
                        val prev = if (i > 0) input[i - 1] else null
                        if (prev != null && (prev.isDigit() || prev == ')' || prev == 'π' || prev == 'e' || prev in supSet)) {
                            val start = i
                            var j = i
                            while (j < input.length && input[j] == '□') j++
                            withStyle(supStyle) { append(input.substring(start, j)) }
                            i = j
                            continue
                        }
                    }

                    // 2) 既に上付き文字（⁰-⁹/⁻）は通常数字に戻して上付き描画（直後の□も同スタイルに含める）
                    val c = input[i]
                    if (c in supSet) {
                        val start = i
                        var j = i
                        while (j < input.length && (input[j] in supSet || input[j] == '□')) j++
                        val segment = input.substring(start, j)
                        withStyle(supStyle) {
                            segment.forEach { sc ->
                                val mapped = when (sc) {
                                    '⁰' -> '0'
                                    '¹' -> '1'
                                    '²' -> '2'
                                    '³' -> '3'
                                    '⁴' -> '4'
                                    '⁵' -> '5'
                                    '⁶' -> '6'
                                    '⁷' -> '7'
                                    '⁸' -> '8'
                                    '⁹' -> '9'
                                    '⁻' -> '-'
                                    else -> sc // 『□』はそのまま描画
                                }
                                append(mapped)
                            }
                        }
                        i = j
                    } else {
                        append(c)
                        i++
                    }
                }
            }
        }

// 安全に3桁区切りを適用（数値以外はそのまま）。
private fun formatNumberWithCommasSafe(text: String): String {
    // 既に"="から始まる形式はここでは来ない（isResultShowingのみで使用）
    // 数値（先頭-許可、小数点任意）かチェック
    val numeric = Regex("^-?\\d+(?:\\.\\d+)?$")
    return if (numeric.matches(text)) {
        try {
            // 14桁以上の大きな整数部は 10 の何乗で表記（例: 1.234567890123×10¹⁴）
            val negative = text.startsWith("-")
            val absText = if (negative) text.substring(1) else text
            val parts = absText.split(".")
            val integerPart = parts[0]
            val decimalPart = if (parts.size > 1) parts[1] else ""
            if (integerPart.length >= 14) {
                formatAsPowerOfTen(negative, integerPart, decimalPart)
            } else {
                val formattedInteger = integerPart.reversed().chunked(3).joinToString(",").reversed()
                val signed = if (negative) "-$formattedInteger" else formattedInteger
                if (decimalPart.isNotEmpty()) "$signed.$decimalPart" else signed
            }
        } catch (_: Exception) {
            text
        }
    } else text
}

// 10の何乗で表記に変換（指数は上付きにする）
private fun formatAsPowerOfTen(isNegative: Boolean, integerPart: String, decimalPart: String): String {
    // 連結した有効桁列（先頭の0は想定なし）
    val digits = integerPart + decimalPart
    val first = digits.firstOrNull() ?: return (if (isNegative) "-" else "") + integerPart
    val rest = digits.drop(1)
    // 有効数字は最大12桁程度に（幅を抑える）。末尾の0は削る
    val mantissaFracRaw = rest.take(12)
    val mantissaFrac = mantissaFracRaw.trimEnd('0')
    val mantissa = buildString {
        if (isNegative) append('-')
        append(first)
        if (mantissaFrac.isNotEmpty()) {
            append('.')
            append(mantissaFrac)
        }
    }
    val exponent = integerPart.length - 1
    val expSup = toSuperscript(exponent)
    return "$mantissa×10$expSup"
}

// 数字を上付き文字に（負号にも対応）
private fun toSuperscript(n: Int): String {
    val map = mapOf(
        '0' to '⁰','1' to '¹','2' to '²','3' to '³','4' to '⁴',
        '5' to '⁵','6' to '⁶','7' to '⁷','8' to '⁸','9' to '⁹'
    )
    val s = n.toString()
    val sb = StringBuilder()
    for (c in s) {
        if (c == '-') sb.append('⁻') else sb.append(map[c] ?: c)
    }
    return sb.toString()
}

@Preview(showBackground = true)
@Composable
fun CalculatorDisplayPreview() {
    MaterialTheme {
        CalculatorDisplay(
            displayText = "1",
            expression = "sin(π/2)+cos(π/2)",
            previewResult = "",
            isResultShowing = true
        )
    }
}
