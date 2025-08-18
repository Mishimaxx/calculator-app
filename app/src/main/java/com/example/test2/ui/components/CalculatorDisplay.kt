package com.example.test2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
        if (text.contains("□") || text.contains("▫")) {
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
        if (text.contains("□") || text.contains("▫")) {
            return text
        }
        
        return when {
            // べき乗記号（数字がない場合のみ）
            text == "²" -> "□²"
            text == "³" -> "□³"
            text == "⁻¹" -> "□⁻¹"
            text == "⁻³" -> "□⁻³"
            
            // 階乗（数字がない場合のみ）
            text == "!" -> "□!"
            
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
            if (expression.isNotEmpty()) {
                val formattedExpression = formatExpressionSpacing(expression)
                val annotatedExpression = buildAnnotatedSuperscripts(formattedExpression, SuperscriptStyle)
                // 式入力時は式を大きく強調、結果表示時は小さく
                val expressionFontSize = calculateFontSize(formattedExpression, if (isResultShowing) 18f else 32f, 30)
                Text(
                    text = annotatedExpression,
                    fontSize = expressionFontSize.sp,
                    color = textColor,
                    textAlign = TextAlign.End,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = FontFamily.Monospace,
                    // 式入力時は太字で強調
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
                        val displayLine = displayLineRaw
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
            val expCharSet = setOf('0','1','2','3','4','5','6','7','8','9','-','⁰','¹','²','³','⁴','⁵','⁶','⁷','⁸','⁹','⁻','□','▫')
            return buildAnnotatedString {
                var i = 0
                while (i < input.length) {
                    // 0-1) ×10^ の後ろは '^' をスキップし、以降を上付き
                    if (i + 4 <= input.length && input.substring(i, i + 4) == "×10^") {
                        append("×10")
                        i += 4 // skip "×10^"
                        val start = i
                        var j = i
                        while (j < input.length && input[j] in expCharSet) j++
                        val segment = input.substring(start, j)
                        withStyle(supStyle) { append(segment) }
                        i = j
                        continue
                    }
                    // 0) ×10 の後ろは上付き
                    if (input.startsWith("×10", i)) {
                        append("×10")
                        i += 3
                        val start = i
                        var j = i
                        while (j < input.length && input[j] in expCharSet) j++
                        val segment = input.substring(start, j)
                        withStyle(supStyle) { append(segment) }
                        i = j
                        continue
                    }
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

                    // 2) 既に上付き文字（⁰-⁹/⁻）は通常数字に戻して上付き描画
                    val c = input[i]
                    if (c in supSet) {
                        val start = i
                        var j = i
                        while (j < input.length && input[j] in supSet) j++
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
                                    else -> sc
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
