package com.example.test2.ui.viewmodel

import android.util.Log

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test2.calculator.CalculationEngine
import com.example.test2.data.model.CalculationEntry
import com.example.test2.data.model.CalculationType
import com.example.test2.data.repository.CalculationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Date

/**
 * 電卓のメインViewModel
 */
class CalculatorViewModel(
    private val repository: CalculationRepository,
    private val calculationEngine: CalculationEngine = CalculationEngine()
) : ViewModel() {

    // UI状態
    private val _displayText = mutableStateOf("")
    val displayText: State<String> = _displayText

    private val _expression = mutableStateOf("")
    val expression: State<String> = _expression

    // 式の直接編集用（キャレット/選択を保持）
    private val _expressionField = mutableStateOf(TextFieldValue(""))
    val expressionField: State<TextFieldValue> = _expressionField

    private val _previewResult = mutableStateOf("")
    val previewResult: State<String> = _previewResult

    private val _calculationType = mutableStateOf(CalculationType.BASIC)
    val calculationType: State<CalculationType> = _calculationType

    private val _isResultShowing = mutableStateOf(false)
    val isResultShowing: State<Boolean> = _isResultShowing

    private val _shouldResetOnNextInput = mutableStateOf(false)
    val shouldResetOnNextInput: State<Boolean> = _shouldResetOnNextInput

    // 履歴データ
    val recentEntries: Flow<List<CalculationEntry>> = repository.getRecentEntries()
    
    // 前回の計算結果を保持
    private var lastResult: String = "0"
    
    // メモリ機能
    private var memoryValue: String = "0"

    // 指数入力プレースホルダーフラグ
    private var isExponentPlaceholderActive = false
    private val exponentPlaceholder = "□"
    private val superscriptPlaceholder = "□" // べき乗用プレースホルダー
    // キーパッド操作後にキャレットを末尾へ移動するフラグ
    private var forceCaretToEnd = false
    // 矢印移動中は末尾強制を抑止
    private var suppressMoveCaretOnPreview = false

    // 現在の選択位置にトークンを挿入（末尾以外）し、選択位置を進める。挿入したらtrue
    private fun insertAtSelection(token: String): Boolean {
        val expr = _expression.value
        val selEnd = _expressionField.value.selection.end.coerceIn(0, expr.length)
        if (selEnd < expr.length) {
            val newExpr = expr.substring(0, selEnd) + token + expr.substring(selEnd)
            _expression.value = newExpr
            // キャレットを進める。プレビュー中は末尾強制しない
            suppressMoveCaretOnPreview = true
            _expressionField.value = TextFieldValue(
                text = newExpr,
                selection = androidx.compose.ui.text.TextRange(selEnd + token.length)
            )
            updatePreviewResult()
            suppressMoveCaretOnPreview = false
            return true
        }
        return false
    }

    // プレビュー用: 左辺が演算子で終わっている場合は除去して安全に評価する
    private fun stripTrailingOperator(s: String): String = s.trimEnd().trimEnd('+','-','*','/','%','×','÷','P','C')
    
    /**
     * 数字を上付き文字に変換
     */
    private fun convertToSuperscript(number: String): String {
        val superscriptMap = mapOf(
            '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
            '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹'
        )
        return number.map { char -> superscriptMap[char] ?: char }.joinToString("")
    }

    /**
     * 表示用に式を変換する（*→×、/→÷、^2→□²、^3→□³）
     */
    private fun formatExpressionForDisplay(expression: String): String {
        var result = expression
            .replace("*", "×")
            .replace("/", "÷")
        
        // べき乗の変換（数字^2 → 数字²、数字^3 → 数字³）
        result = result.replace(Regex("(\\d+)\\^2")) { matchResult ->
            "${matchResult.groupValues[1]}²"
        }
        result = result.replace(Regex("(\\d+)\\^3")) { matchResult ->
            "${matchResult.groupValues[1]}³"
        }
        result = result.replace(Regex("(\\d+)\\^-1")) { matchResult ->
            "${matchResult.groupValues[1]}⁻¹"
        }
        result = result.replace(Regex("(\\d+)\\^-3")) { matchResult ->
            "${matchResult.groupValues[1]}⁻³"
        }
        
        // 未入力の場合は□を表示
        result = result.replace("^2", "□²")
        result = result.replace("^3", "□³")
        result = result.replace("^-1", "□⁻¹")
        result = result.replace("^-3", "□⁻³")
        
        // 指数の変換（10^x と e^x）
        result = result.replace(Regex("10\\^(\\d+)")) { matchResult ->
            val exponent = matchResult.groupValues[1]
            "10${convertToSuperscript(exponent)}"
        }
        result = result.replace(Regex("e\\^(\\d+)")) { matchResult ->
            val exponent = matchResult.groupValues[1]
            "e${convertToSuperscript(exponent)}"
        }
        // まだ指数未入力のときは上付きプレースホルダーを表示
        result = result.replace("10^", "10□")
        result = result.replace("e^", "e□")
        
    // 階乗の未入力の場合のみ□を表示
    // 直前が「数字/)/上付き数字/π/e」のいずれでもない場合に限り、! を □! と表示
    // 例: "!" や "+!"、"( + !" → □!、一方で "9!"、"(9)!"、"π!"、"e!"、"9²!" はそのまま
    result = result.replace(Regex("(?<![\\d)⁰¹²³⁴⁵⁶⁷⁸⁹πe])!")) { "□!" }
        
        // 一般の整数べき（例: 9^9 → 9⁹）
        result = result.replace(Regex("(\\d+(?:\\.\\d+)?)\\^(\\d+)")) { m ->
            val base = m.groupValues[1]
            val exp = convertToSuperscript(m.groupValues[2])
            base + exp
        }
        
        // 一般べき乗の未入力（例: 9^ → 9□）
        result = result.replace(Regex("(\\d+(?:\\.\\d+)?)\\^$")) { m ->
            val base = m.groupValues[1]
            "$base$superscriptPlaceholder"
        }
        
        // 任意の残った^を隠す（すべての^記号を除去）
        result = result.replace("^", "")
        
        return result
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

    /**
     * 式全体をフォーマットする（演算子変換と数字の3桁区切り）
     */
    private fun formatCompleteExpression(expression: String): String {
        var result = expression
            .replace("*", "×")
            .replace("/", "÷")
        
        // べき乗の変換（数字^2 → 数字²、数字^3 → 数字³）
        result = result.replace(Regex("(\\d+)\\^2")) { matchResult ->
            "${matchResult.groupValues[1]}²"
        }
        result = result.replace(Regex("(\\d+)\\^3")) { matchResult ->
            "${matchResult.groupValues[1]}³"
        }
        result = result.replace(Regex("(\\d+)\\^-1")) { matchResult ->
            "${matchResult.groupValues[1]}⁻¹"
        }
        result = result.replace(Regex("(\\d+)\\^-3")) { matchResult ->
            "${matchResult.groupValues[1]}⁻³"
        }
        
        // 未入力の場合は□を表示
        result = result.replace("^2", "□²")
        result = result.replace("^3", "□³")
        result = result.replace("^-1", "□⁻¹")
        result = result.replace("^-3", "□⁻³")
        
    // 平方根・立方根はプレースホルダーを強制しない（ViewModelの入力ロジックで制御）
        
        // 指数の変換（10^x と e^x）
        result = result.replace(Regex("10\\^(\\d+)")) { matchResult ->
            val exponent = matchResult.groupValues[1]
            "10${convertToSuperscript(exponent)}"
        }
        result = result.replace(Regex("e\\^(\\d+)")) { matchResult ->
            val exponent = matchResult.groupValues[1]
            "e${convertToSuperscript(exponent)}"
        }
        // まだ指数未入力のときは上付きプレースホルダーを表示
        result = result.replace("10^", "10□")
        result = result.replace("e^", "e□")
        
    // 階乗の未入力の場合のみ□を表示
    // 直前が「数字/)/上付き数字/π/e」のいずれでもない場合に限り、! を □! と表示
    result = result.replace(Regex("(?<![\\d)⁰¹²³⁴⁵⁶⁷⁸⁹πe])!")) { "□!" }
        
        // 一般の整数べき（例: 9^9 → 9⁹）
        // 負の指数は除外（^-1, ^-3は既に処理済み）
        result = result.replace(Regex("(\\d+(?:\\.\\d+)?)\\^([1-9]\\d*)")) { m ->
            val base = m.groupValues[1]
            val exp = convertToSuperscript(m.groupValues[2])
            base + exp
        }
        
        // 一般べき乗の未入力（例: 9^ → 9□）
        result = result.replace(Regex("(\\d+(?:\\.\\d+)?)\\^$")) { m ->
            val base = m.groupValues[1]
            "$base$superscriptPlaceholder"
        }
        
        // 任意の残った^を隠す（すべての^記号を除去）
        result = result.replace("^", "")

        // 数字部分のみを3桁区切りにフォーマット（途中入力の末尾ドットも維持）
        return result.replace(Regex("-?\\d+(?:\\.\\d+)?")) { matchResult ->
            formatNumberWithCommasKeepingDot(matchResult.value)
        }
    }

    /**
     * 表示用の式を取得
     */
    val displayExpression: String
        get() = formatCompleteExpression(_expression.value)

    /**
     * 表示用のプレビュー結果を取得
     */
    val formattedPreviewResult: String
        get() {
            val result = _previewResult.value.trim()
            // 先頭に=がある場合も、数値部分を3桁区切り
            if (result.startsWith("=")) {
                val tail = result.removePrefix("=").trimStart()
                return if (tail.matches(Regex("^-?\\d+(?:\\.\\d+)?$"))) {
                    "= " + formatPreviewNumberSmart(tail)
                } else {
                    result
                }
            }
            // 途中のプレビューでも、数値トークンだけ3桁区切りを適用（式や記号はそのまま）
            val withGroupedNumbers = result.replace(Regex("-?\\d+(?:\\.\\d+)?")) { m ->
                formatNumberWithCommasKeepingDot(m.value)
            }
            return withGroupedNumbers
        }

    // プレビューの数値を、14桁以上なら ×10ⁿ 表記、それ以外は3桁区切り
    private fun formatPreviewNumberSmart(num: String): String {
        return try {
            val negative = num.startsWith("-")
            val abs = if (negative) num.substring(1) else num
            val parts = abs.split(".")
            val intPart = parts[0]
            val dec = if (parts.size > 1) parts[1] else ""
            if (intPart.length >= 14) {
                // ViewModel側でも簡易的に 10^ 表記へ。指数は上付きに変換
                val mantissaDigits = (intPart + dec)
                val first = mantissaDigits.firstOrNull() ?: return num
                val rest = mantissaDigits.drop(1)
                val frac = rest.take(12).trimEnd('0')
                val mantissa = buildString {
                    if (negative) append('-')
                    append(first)
                    if (frac.isNotEmpty()) append('.').append(frac)
                }
                val exp = intPart.length - 1
                val sup = convertToSuperscript(exp.toString())
                "$mantissa×10$sup"
            } else {
                formatNumberWithCommas(num)
            }
        } catch (_: Exception) { num }
    }

    /**
     * 表示用の計算結果を取得（エラーメッセージは3桁区切りしない）
     */
    val formattedDisplayText: String
        get() {
            val value = _displayText.value
            // 数値のみ（符号と小数点を許容）。小数点の直後まで入力中（末尾が'.'）でも3桁区切りを適用する
            val numericLike = value.matches(Regex("^-?\\d+(?:\\.\\d*)?$"))
            return if (numericLike) formatNumberWithCommasKeepingDot(value) else value
        }

    // 表示用: 末尾が '.' の途中入力でも小数点を保持したまま3桁区切りにする
    private fun formatNumberWithCommasKeepingDot(text: String): String {
        if (text.isEmpty()) return text
        val negative = text.startsWith("-")
        val raw = if (negative) text.substring(1) else text
        val hasDot = raw.contains('.')
        val parts = raw.split('.', limit = 2)
        val integerPart = parts[0]
        val decimalPart = if (hasDot) parts.getOrNull(1) ?: "" else null
        val formattedInteger = try {
            integerPart.reversed().chunked(3).joinToString(",").reversed()
        } catch (_: Exception) { integerPart }
        return buildString {
            if (negative) append('-')
            append(formattedInteger)
            if (hasDot) {
                append('.')
                if (decimalPart != null) append(decimalPart)
            }
        }
    }

    /**
     * 式のプレビュー結果を更新する
     */
    private fun updatePreviewResult() {
        // 評価前に安全化：内部式に×/÷が混入しても必ず*/へ正規化
        val rawExpr = _expression.value
        val expr = rawExpr.replace('×', '*').replace('÷', '/')
        // 直ちに評価してはいけない明確な未完条件を早期リターン
        val trimmedExpr = expr.trim()
        fun syncExpressionFieldCaret() {
            if (_expressionField.value.text != _expression.value || forceCaretToEnd) {
                val newText = _expression.value
                val keepSel = suppressMoveCaretOnPreview && !forceCaretToEnd
                val newSel = if (forceCaretToEnd && !keepSel) newText.length else _expressionField.value.selection.end.coerceIn(0, newText.length)
                _expressionField.value = _expressionField.value.copy(text = newText, selection = androidx.compose.ui.text.TextRange(newSel))
                if (!suppressMoveCaretOnPreview) forceCaretToEnd = false
            }
        }
        // 未入力プレースホルダ、演算子末尾、開き括弧過多はプレビューを出さない
        val openParens = trimmedExpr.count { it == '(' }
        val closeParens = trimmedExpr.count { it == ')' }
        if (trimmedExpr.isEmpty() ||
            trimmedExpr.endsWith('+') || trimmedExpr.endsWith('-') ||
            trimmedExpr.endsWith('*') || trimmedExpr.endsWith('/') ||
            trimmedExpr.endsWith('%') || trimmedExpr.endsWith('P') || trimmedExpr.endsWith('C') ||
            trimmedExpr.contains('□') || trimmedExpr.contains('□') ||
            openParens > closeParens) {
            _previewResult.value = ""
            // 入力欄の同期だけは継続
            if (_expressionField.value.text != _expression.value || forceCaretToEnd) {
                val newText = _expression.value
                val keepSel = suppressMoveCaretOnPreview && !forceCaretToEnd
                val newSel = if (forceCaretToEnd && !keepSel) newText.length else _expressionField.value.selection.end.coerceIn(0, newText.length)
                _expressionField.value = _expressionField.value.copy(text = newText, selection = androidx.compose.ui.text.TextRange(newSel))
                if (!suppressMoveCaretOnPreview) forceCaretToEnd = false
            }
            return
        }
        
    if (expr.isNotEmpty() && !_isResultShowing.value) {
            // 平方根入力のプレビュー制御（³√やˣ√は対象外）
            val trimmed = expr.trim()
            val lastSqrtIndex = trimmed.lastIndexOf('√')
            if (lastSqrtIndex != -1 && (lastSqrtIndex == 0 || trimmed.getOrNull(lastSqrtIndex - 1) != '³')) {
                // 直前が上付きの任意指数（例: ˣ√）の場合は専用処理に委ねる
                val prevChar = trimmed.getOrNull(lastSqrtIndex - 1)
                if (prevChar != null && "⁰¹²³⁴⁵⁶⁷⁸⁹".contains(prevChar)) {
                    // スキップ（ˣ√のときはここで扱わない）
                } else {
                val after = trimmed.substring(lastSqrtIndex + 1)
                // 1) 末尾がプレースホルダ → √□ を維持
                if (after.startsWith('□') && lastSqrtIndex + 1 == trimmed.length - 1) {
                    _previewResult.value = "√□"
                    return
                }
                // 2) 式全体が √<数字> の形 → 即評価して =結果
                val wholeSqrt = Regex("^√(\\d+(?:\\.\\d*)?)$")
                val mWhole = wholeSqrt.find(trimmed)
                if (mWhole != null) {
                    try {
                        val result = calculationEngine.evaluate(trimmed)
                        if (!result.contains("Error") && result != "Division by Zero" &&
                            result != "Overflow Error" && result != "Math Error") {
                            _previewResult.value = "=" + result
                        } else {
                            _previewResult.value = ""
                        }
                    } catch (_: Exception) {
                        _previewResult.value = ""
                    }
                    return
                }
                // 3) それ以外は通常判定へ（例えば 2+√9 は全体評価に委ねる）
                }
            }
            // 階乗入力のプレビュー制御
            if (trimmed.endsWith('!')) {
                // a) 式全体が <数値>! の形 → 即評価して =結果
                val wholeFact = Regex("^\\d+(?:\\.\\d*)?!$")
                if (wholeFact.matches(trimmed)) {
                    try {
                        val result = calculationEngine.evaluate(trimmed)
                        if (!result.contains("Error") && result != "Division by Zero" &&
                            result != "Overflow Error" && result != "Math Error") {
                            _previewResult.value = "=" + result
                        } else {
                            _previewResult.value = ""
                        }
                    } catch (_: Exception) {
                        _previewResult.value = ""
                    }
                    return
                }
                // b) オペランド未確定（例: "!", "3+!"）→ プレースホルダ "=□!" を表示
                val prevChar = trimmed.dropLast(1).lastOrNull()
                val isValidBase = prevChar != null && (
                    prevChar.isDigit() || prevChar == ')' ||
                    "⁰¹²³⁴⁵⁶⁷⁸⁹".contains(prevChar) || prevChar == 'π' || prevChar == 'e'
                )
                if (!isValidBase) {
                    _previewResult.value = "=□!"
                    return
                }
            }
            // 10^x の指数未入力時はシンプルに "10□" を表示（冒頭の 0× を出さない）
            if (trimmed.endsWith("10^") ) {
                _previewResult.value = "10□"
                syncExpressionFieldCaret()
                return
            }
            // *10^ の直後（EXP）も同様に扱う
            if (trimmed.endsWith("*10^") ) {
                // 左辺(乗算の前)を評価して、mantissa×10□ を表示
                val left = trimmed.removeSuffix("*10^")
                val base = stripTrailingOperator(left)
                val mantissa = if (base.isBlank()) {
                    "0"
                } else try {
                    val r = calculationEngine.evaluate(base)
                    if (r.matches(Regex("^-?\\d+(?:\\.\\d+)?$"))) formatPreviewNumberSmart(r) else base
                } catch (_: Exception) { base }
                _previewResult.value = "$mantissa×10□"
                syncExpressionFieldCaret()
                return
            }
            if (trimmed.endsWith("e^") ) {
                _previewResult.value = "e□"
                syncExpressionFieldCaret()
                return
            }
            // x^y の指数が未入力（^ または ^- のみ）なら、基数＋上付きプレースホルダーをプレビュー
            run {
                val lastCaret = trimmed.lastIndexOf('^')
                if (lastCaret != -1) {
                    val after = if (lastCaret + 1 <= trimmed.length - 1) trimmed.substring(lastCaret + 1) else ""
                    val before = trimmed.substring(0, lastCaret)
                    val baseChar = before.lastOrNull()
                    val hasBase = baseChar != null && (baseChar.isDigit() || baseChar == ')' || baseChar == 'π' || baseChar == 'e' || "⁰¹²³⁴⁵⁶⁷⁸⁹".contains(baseChar))
                    if (hasBase && (after.isEmpty() || after == "-")) {
                        // 表示用に caret までを変換（<base>^ → <base>□）
                        var disp = formatExpressionForDisplay(trimmed.substring(0, lastCaret + 1))
                        if (after == "-") {
                            disp = if (disp.endsWith("□")) disp.dropLast(1) + "⁻□" else disp + "⁻□"
                        }
                        _previewResult.value = disp
                        syncExpressionFieldCaret()
                        return
                    }
                }
            }
            // xʸ の指数が未入力（□/⁻□）のときは、基数＋プレースホルダーをプレビューに出す（例: 9□ / 9⁻□）
            if (trimmed.endsWith("⁻□") || trimmed.endsWith("□")) {
                _previewResult.value = trimmed
                syncExpressionFieldCaret()
                return
            }
            try {
            // e^x形式やべき乗の中間結果をチェック（例：e^2、10^3、9^2、5^3など）
            // べきの中間結果: ^ を使う形と、上付きのみの形の両方に対応
            val powerPatternCaret = Regex("(e|10|\\d+)\\^(-?[\\d.]+)\$")
            val powerMatchCaret = powerPatternCaret.find(expr)
            val powerPatternSup = Regex("(e|10|\\d+)[⁻]?[⁰¹²³⁴⁵⁶⁷⁸⁹]+\$")
            val powerMatchSup = powerPatternSup.find(expr)
            
            if (powerMatchCaret != null || powerMatchSup != null) {
                try {
                    val result = calculationEngine.evaluate(expr)
                    if (!result.contains("Error") && result != "Division by Zero" &&
                        result != "Overflow Error" && result != "Math Error") {
                        // べき乗の簡単な形（e^x, 10^x, a^b）は完了時に = を付けて表示
                        _previewResult.value = "=" + result
                    } else {
                        _previewResult.value = ""
                    }
                } catch (e: Exception) {
                    _previewResult.value = ""
                }
            }
            // 関数呼び出しの中間結果をチェック（例：sin(30）、log(100）など）
            else if (expr.matches(Regex("(sin|cos|tan|asin|acos|atan|sinh|cosh|tanh|ln|log|sqrt|cbrt)\\(([\\d.]+)\\)?\$"))) {
                val functionPattern = Regex("(sin|cos|tan|asin|acos|atan|sinh|cosh|tanh|ln|log|sqrt|cbrt)\\(([\\d.]+)\\)?\$")
                val functionMatch = functionPattern.find(expr)
                
                if (functionMatch != null) {
                    val funcName = functionMatch.groupValues[1]
                    val argStr = functionMatch.groupValues[2]
                    if (argStr.isNotEmpty()) {
                        try {
                            // 関数に引数がある場合、中間結果を計算
                            val tempExpr = if (expr.endsWith(")")) {
                                expr // 既に閉じ括弧がある場合はそのまま
                            } else {
                                expr + ")" // 閉じ括弧を追加
                            }
                            val result = calculationEngine.evaluate(tempExpr)
                            if (!result.contains("Error") && result != "Division by Zero" &&
                                result != "Overflow Error" && result != "Math Error") {
                                _previewResult.value = result
                            } else {
                                _previewResult.value = ""
                            }
                        } catch (e: Exception) {
                            _previewResult.value = ""
                        }
                    } else {
                        _previewResult.value = ""
                    }
                }
            }
            // 演算子を含む完全な式の場合（例：「33+22」、「9P6」、「10C4」、「√9」）
        else if ((expr.contains('+') || expr.contains('-') || expr.contains('*') ||
                 expr.contains('/') || expr.contains('%') || expr.contains('P') || expr.contains('C') ||
              (expr.contains('√') && !expr.contains('□')) || expr.contains('!')) &&
            !expr.trim().endsWith('+') && !expr.trim().endsWith('-') &&
            !expr.trim().endsWith('*') && !expr.trim().endsWith('/') &&
            !expr.trim().endsWith('%') && !expr.trim().endsWith('P') && !expr.trim().endsWith('C') &&
            !expr.trim().endsWith('√') && !expr.trim().endsWith(' ') &&
            openParens == closeParens) {
                val result = calculationEngine.evaluate(expr)
                if (!result.contains("Error") && result != "Division by Zero" &&
                    result != "Overflow Error" && result != "Math Error") {
                    _previewResult.value = "=$result"
                } else {
                    _previewResult.value = "" // 3÷0などのエラー時は何も表示しない
                }
            }
            // 演算子で終わっている場合（例：「33+」、「9P」、「10C」、「√□」）は演算子の前の数値を表示
            else if (expr.trim().endsWith('+') || expr.trim().endsWith('-') || 
                     expr.trim().endsWith('*') || expr.trim().endsWith('/') || 
                     expr.trim().endsWith('%') || expr.trim().endsWith('P') || expr.trim().endsWith('C') ||
                     expr.trim().endsWith("√□") || expr.trim().endsWith("³√□") || expr.trim().endsWith(' ')) {
                // 演算子の前の数値を取得
                val trimmedExpr = expr.trim()
                val lastOperatorIndex = maxOf(
                    trimmedExpr.lastIndexOf('+'),
                    trimmedExpr.lastIndexOf('-'),
                    trimmedExpr.lastIndexOf('*'),
                    trimmedExpr.lastIndexOf('/'),
                    trimmedExpr.lastIndexOf('%'),
                    trimmedExpr.lastIndexOf('P'),
                    trimmedExpr.lastIndexOf('C'),
                    if (trimmedExpr.endsWith("√□")) trimmedExpr.lastIndexOf("√□") else -1,
                    if (trimmedExpr.endsWith("³√□")) trimmedExpr.lastIndexOf("³√□") else -1
                )
                
                if (lastOperatorIndex > 0) {
                    // 演算子より前の部分を評価
                    val beforeOperator = trimmedExpr.substring(0, lastOperatorIndex).trim()
                    if (beforeOperator.isNotEmpty()) {
                        try {
                            val result = calculationEngine.evaluate(beforeOperator)
                            if (!result.contains("Error") && result != "Division by Zero" && 
                                result != "Overflow Error") {
                                _previewResult.value = result
                            } else {
                                _previewResult.value = beforeOperator
                            }
                        } catch (e: Exception) {
                            _previewResult.value = beforeOperator
                        }
                    } else {
                        _previewResult.value = ""
                    }
                } else {
                    // 最初の演算子の場合（例：「33+」で33を表示）
                    val numberPart = if (trimmedExpr.endsWith(' ')) {
                        trimmedExpr.trim()
                    } else {
                        trimmedExpr.dropLast(1).trim()
                    }
                    if (numberPart.isNotEmpty()) {
                        _previewResult.value = numberPart
                    } else {
                        _previewResult.value = ""
                    }
                }
            }
            // 数字のみの場合は現在の数字を表示
            else {
                // 単独の数値（負号や末尾の小数点含む）ならそのまま表示
                if (expr.isNotEmpty() && expr.matches(Regex("-?\\d+(?:\\.\\d*)?"))) {
                    _previewResult.value = expr
                } else {
                    _previewResult.value = ""
                }
            }
            } catch (e: Exception) {
                _previewResult.value = ""
            }
        } else {
            _previewResult.value = ""
        }
        // 式フィールドの同期（テキストがズレていたら反映。選択は末尾へ寄せる）
        if (_expressionField.value.text != _expression.value || forceCaretToEnd) {
            val newText = _expression.value
            val newSel = if (forceCaretToEnd) newText.length else newText.length.coerceAtMost(_expressionField.value.selection.end)
            _expressionField.value = _expressionField.value.copy(text = newText, selection = androidx.compose.ui.text.TextRange(newSel))
            forceCaretToEnd = false
    }
    }

    private fun syncPreviewAndMoveCaretEnd() {
        forceCaretToEnd = true
        // 直後にプレビュー更新しつつ、キャレット末尾へ寄せる
        updatePreviewResult()
    }

    /**
     * 直接編集（キーボード入力や貼り付け）
     */
    fun onExpressionEdited(newValue: TextFieldValue) {
    _expressionField.value = newValue
        _expression.value = newValue.text
        _isResultShowing.value = false
        _shouldResetOnNextInput.value = false
    // 直接編集では現在の選択を尊重し、末尾移動はしない
    updatePreviewResult()
    }

    /**
     * 数字ボタンが押された時の処理
     */
    fun onNumberClicked(number: String) {
        // 1) 直前が結果表示なら、新しい計算として開始（以降の特殊分岐に入らない）
        if (_isResultShowing.value) {
            _displayText.value = number
            _expression.value = number
            _isResultShowing.value = false
            _shouldResetOnNextInput.value = false
            syncPreviewAndMoveCaretEnd()
            return
        }
        // 1.5) 直前に演算子を押している場合は、最優先で新しいオペランドとして処理
        if (_shouldResetOnNextInput.value) {
            // 表示は新しい数に、内部式は末尾に数字を連結
            _displayText.value = number
            _expression.value += number
            _shouldResetOnNextInput.value = false
            syncPreviewAndMoveCaretEnd()
            return
        }
        // 階乗の特別処理が必要かチェック
        val needsFactorialHandling = _expression.value.isNotEmpty() && 
            (_expression.value.endsWith("□!") || (_expression.value.endsWith("!") && !_expression.value.endsWith("□!")))
        
        // 先に矢印で動かした位置への挿入を試みる（末尾でない場合のみ）。指数プレースホルダ中と階乗処理が必要な場合は除外
        if (!isExponentPlaceholderActive && !needsFactorialHandling) {
            if (insertAtSelection(number)) return
        }
        // xroot（ˣ√□）の最初の入力は指数xに割り当てる
        run {
            if (isExponentPlaceholderActive) {
                val expr0 = _expression.value
                val disp0 = _displayText.value
                val idxSqrt = expr0.indexOf('√')
                val idxSup = expr0.indexOf('□')
                if (idxSqrt != -1 && idxSup != -1 && idxSup < idxSqrt && expr0.contains("√□")) {
                    val supers = convertToSuperscript(number)
                    _expression.value = expr0.replaceFirst("□", supers)
                    _displayText.value = disp0.replaceFirst("□", supers)
                    // ひとまず指数は1桁のみとし、次の入力は被開平数（□）へ
                    isExponentPlaceholderActive = false
                    syncPreviewAndMoveCaretEnd()
                    return
                }
            }
        }
        // べき乗（^2, ^3, ^-1, ^-3）を基数なしで置いた場合、最初に来た数字で基数を埋める
        // 例: 「9 × ^2」や「^3」などに対して、押下数字を直前に挿入する
        run {
            val expr = _expression.value
            if (expr.isNotEmpty()) {
                val tokenRegex = Regex("\\^(?:2|3|-1|-3)")
                val unmatched = tokenRegex.findAll(expr)
                    .map { it.range.first }
                    .firstOrNull { pos ->
                        val prev = if (pos > 0) expr.getOrNull(pos - 1) else null
                        !(prev != null && (prev.isDigit() || prev == ')' || prev == 'π' || prev == 'e'))
                    }
                if (unmatched != null) {
                    val pos = unmatched
                    _expression.value = expr.substring(0, pos) + number + expr.substring(pos)

                    // 表示側も「□」最優先、その後 上付きの直前（基数なし）へ挿入
                    val disp0 = _displayText.value
                    _displayText.value = when {
                        disp0.contains("□") -> disp0.replaceFirst("□", number)
                        disp0.contains("□") -> disp0.replaceFirst("□", convertToSuperscript(number))
                        else -> {
                            // 基数なし上付きの前を探す
                            val candidates = mutableListOf<Int>()
                            var i = 0
                            while (i < disp0.length) {
                                when {
                                    i + 1 < disp0.length && disp0.startsWith("⁻¹", i) -> { candidates.add(i); i += 2; continue }
                                    i + 1 < disp0.length && disp0.startsWith("⁻³", i) -> { candidates.add(i); i += 2; continue }
                                    disp0[i] == '²' || disp0[i] == '³' -> { candidates.add(i); i += 1; continue }
                                    else -> i += 1
                                }
                            }
                            val insertAt = candidates.firstOrNull() ?: -1
                            if (insertAt >= 0) disp0.substring(0, insertAt) + number + disp0.substring(insertAt) else number + disp0
                        }
                    }
                    syncPreviewAndMoveCaretEnd()
                    return
                }
            }
        }
        // 末尾が「<数字>^<何か（指数の断片）>」のときは、追加の数字は ^ の直前に差し込み、基数を伸長
        // ただし指数未入力（^のみ、または指数プレースホルダー中）はスキップして指数へ入れる
        run {
            val expr2 = _expression.value
            val caretIndex = expr2.lastIndexOf('^')
            val hasCaret = caretIndex >= 0
            val hasExponentFragment = hasCaret && caretIndex < expr2.length - 1 // '^' の後ろに何かある
            // 式全体が base^exponent（オプションで負号つき数字）だけのときに限り基数拡張を許可
            val m = Regex("^\\d+(?:\\.\\d+)?\\^-?\\d+$").find(expr2)
            // '^' の後ろに四則やPC演算子が現れているなら、今は別オペランドを編集中なので基数拡張はしない
            val operatorAfterCaret = if (caretIndex >= 0) {
                listOf(" + ", " - ", " * ", " / ", " % ", " P ", " C ")
                    .any { op ->
                        val idx = expr2.indexOf(op, caretIndex + 1)
                        idx != -1
                    }
            } else false
            if (!isExponentPlaceholderActive && hasExponentFragment && m != null && !operatorAfterCaret) {
                val caret = expr2.lastIndexOf('^').coerceAtLeast(0)
                _expression.value = expr2.substring(0, caret) + number + expr2.substring(caret)
                _displayText.value = formatExpressionForDisplay(_expression.value)
                val newText = _expression.value
                _expressionField.value = TextFieldValue(
                    text = newText,
                    selection = androidx.compose.ui.text.TextRange(newText.length)
                )
                syncPreviewAndMoveCaretEnd()
                return
            }
        }
        
        // 階乗の場合の特別処理（最優先で処理）
        if (_expression.value.isNotEmpty()) {
            val expr = _expression.value
            if (expr.endsWith("□!")) {
                // プレースホルダの基数を数字で置換（□→数字）
                _expression.value = expr.removeSuffix("□!") + number + "!"
                _displayText.value = formatExpressionForDisplay(_expression.value)
                // TextField も即同期
                run {
                    val newText = _expression.value
                    _expressionField.value = TextFieldValue(
                        text = newText,
                        selection = androidx.compose.ui.text.TextRange(newText.length)
                    )
                }
                syncPreviewAndMoveCaretEnd()
                return
            } else if (expr.endsWith("!")) {
                // 既に n! の末尾に数字を入力したら、n を拡張（末尾!の直前に数字を挿入）
                val base = expr.dropLast(1)
                _expression.value = base + number + "!"
                _displayText.value = formatExpressionForDisplay(_expression.value)
                // TextField も即同期
                run {
                    val newText = _expression.value
                    _expressionField.value = TextFieldValue(
                        text = newText,
                        selection = androidx.compose.ui.text.TextRange(newText.length)
                    )
                }
                syncPreviewAndMoveCaretEnd()
                return
            }
        }
        // べき乗の□（上付きプレースホルダー）を最優先で埋める: '^' 系の指数入力中なら必ず指数に数字を入れる
        run {
            val exprNow = _expression.value
            // 末尾が 10^ / e^ / *10^ か、最後の '^' の後ろが未入力/符号のみ/数字のみの状態なら指数入力中とみなす
            val lastCaret = exprNow.lastIndexOf('^')
            val afterCaret = if (lastCaret >= 0 && lastCaret < exprNow.length - 1) exprNow.substring(lastCaret + 1) else ""
            val inPowerTail = lastCaret != -1 && (
                // '^' 直後（末尾が '^'）または '^' の後が "-" のみ、または "-?数字*"
                lastCaret == exprNow.length - 1 || afterCaret == "-" || afterCaret.matches(Regex("-?\\d*"))
            )
            if (isExponentPlaceholderActive ||
                exprNow.endsWith("10^") || exprNow.endsWith("e^") || exprNow.endsWith("*10^") || inPowerTail
            ) {
                // 内部式は '^' の後に通常数字を連結。表示は変換関数で上付きへ。
                _expression.value = exprNow + number
                // 表示は□があれば最初の□を上付き数字に置換、なければ全体変換
                val sup = convertToSuperscript(number)
                _displayText.value = if (_displayText.value.contains("□")) {
                    _displayText.value.replaceFirst("□", sup)
                } else {
                    formatExpressionForDisplay(_expression.value)
                }
                // 入力欄も同期（キャレットは末尾）
                val newText = _expression.value
                _expressionField.value = TextFieldValue(
                    text = newText,
                    selection = androidx.compose.ui.text.TextRange(newText.length)
                )
                // 指数入力は継続
                syncPreviewAndMoveCaretEnd()
                return
            }
        }

        // □や□が存在する場合は式側を優先して最初のものを数字で置換（階乗の□!は除外）
        if (_expression.value.contains("□") && !_expression.value.endsWith("□!")) {
            _expression.value = _expression.value.replaceFirst("□", number)
            // 表示側も必ず最初の□/□を数字にする（『後ろに付く』のを防ぐ）
            _displayText.value = when {
                _displayText.value.contains("□") -> _displayText.value.replaceFirst("□", number)
                _displayText.value.contains("□") -> _displayText.value.replaceFirst("□", convertToSuperscript(number))
                else -> _displayText.value + number
            }
            isExponentPlaceholderActive = false
            syncPreviewAndMoveCaretEnd()
            return
        } else if (isExponentPlaceholderActive ||
            _expression.value.endsWith("10^") || _expression.value.endsWith("e^") || _expression.value.endsWith("*10^") ||
            _expression.value.contains("□") ) {
            // 上付きの指数入力中（初回: □ を上付き数字に置換、以降: 上付き数字を末尾に追加）
            try {
                val supers = convertToSuperscript(number)
                val currentDisplay = _displayText.value
                val currentExpression = _expression.value
                
                _displayText.value = when {
                    currentDisplay.contains("□") -> currentDisplay.replaceFirst("□", supers)
                    else -> currentDisplay + supers
                }
                // 内部式の更新: caret系(10^/e^/*10^) と x^y どちらも '^' の後に通常数字を連結
                _expression.value = when {
                    currentExpression.endsWith("10^") || currentExpression.endsWith("e^") || currentExpression.endsWith("*10^") ->
                        currentExpression + number
                    currentExpression.contains("^") ->
                        currentExpression + number
                    currentExpression.contains("□") ->
                        currentExpression.replaceFirst("□", supers) // 後方互換: 旧プレースホルダー
                    else -> currentExpression + supers
                }
                // 指数入力中は継続（フラグは維持）
                // プレビュー結果を更新
                syncPreviewAndMoveCaretEnd()
                return
            } catch (e: Exception) {
                // フォールバック: 通常の数字入力として処理
                Log.w("Calculator", "Error in superscript handling: ${e.message}")
            }
        }

        if (_isResultShowing.value) {
            // 計算結果が表示されている場合、新しい計算を開始
            _displayText.value = number
            _expression.value = number
            _isResultShowing.value = false
            _shouldResetOnNextInput.value = false
        } else if (_shouldResetOnNextInput.value) {
            // 演算子を押した後の最初の数字入力
            _displayText.value = number
            _expression.value += number
            _shouldResetOnNextInput.value = false
        } else {
            // 通常の数字入力
            if (_displayText.value.isEmpty() || _displayText.value == "0") {
                _displayText.value = number
                if (_expression.value.isEmpty()) {
                    _expression.value = number
                } else {
                    _expression.value += number
                }
            } else {
                _displayText.value += number
                _expression.value += number
            }
        }
        // プレビュー結果を更新
        // display と expression の最終整合（表示は内部式から×/÷に置換した簡易同期）
        try {
            if (_displayText.value.isEmpty() && _expression.value.isNotEmpty()) {
                _displayText.value = _expression.value.replace('*','×').replace('/','÷')
            }
        } catch (_: Exception) {}
        syncPreviewAndMoveCaretEnd()
    }

    /**
     * 演算子ボタンが押された時の処理
     */
    fun onOperatorClicked(operator: String) {
        try {
            // 内部式で使う演算子（評価エンジン用、×/÷は*/に正規化）
            val internalOperator = when (operator) {
                "×" -> "*"
                "÷" -> "/"
                "nPr" -> "P"
                "nCr" -> "C"
                else -> operator
            }
            // 先にキャレット位置挿入（末尾以外）に対応：基本四則のみ適用
            if (internalOperator in listOf("+","-","*","/")) {
                val expr = _expression.value
                val pos = _expressionField.value.selection.end.coerceIn(0, expr.length)
                if (pos < expr.length) {
                    val token = " $internalOperator "
                    val newExpr = expr.substring(0, pos) + token + expr.substring(pos)
                    _expression.value = newExpr
                    suppressMoveCaretOnPreview = true
                    _expressionField.value = TextFieldValue(
                        text = newExpr,
                        selection = androidx.compose.ui.text.TextRange(pos + token.length)
                    )
                    updatePreviewResult()
                    suppressMoveCaretOnPreview = false
                    _shouldResetOnNextInput.value = true
                    return
                }
            }

            // 表示で使う演算子
            val displayOperator = when (operator) {
                "nPr" -> "P"
                "nCr" -> "C"
                else -> operator
            }

            // X^y 入力中に「-」が押された場合は指数の符号として扱う
            if (isExponentPlaceholderActive && operator == "-") {
                if (!_displayText.value.contains("⁻□")) {
                    _displayText.value = _displayText.value.replaceFirst("□", "⁻□")
                    _expression.value = _expression.value.replaceFirst("□", "⁻□")
                }
                _shouldResetOnNextInput.value = false
                syncPreviewAndMoveCaretEnd()
                return
            }

            // プレースホルダーが有効な場合は、それを閉じてから演算子を追加
            if (isExponentPlaceholderActive) {
                if (_expression.value.endsWith("□")) {
                    _expression.value = _expression.value.dropLast(1)
                }
                if (_displayText.value.endsWith("□")) {
                    _displayText.value = _displayText.value.dropLast(1)
                }
                isExponentPlaceholderActive = false
            }

            // nPr と nCr の場合は数字チェック
            if (operator == "nPr" || operator == "nCr") {
                val currentDisplay = _displayText.value
                if (currentDisplay.isEmpty() || currentDisplay == "0" || 
                    currentDisplay.toDoubleOrNull() == null) {
                    _displayText.value = "数字から入力してください"
                    _expression.value = ""
                    _isResultShowing.value = true
                    _shouldResetOnNextInput.value = false
                    syncPreviewAndMoveCaretEnd()
                    return
                }
            }

            // 現在の式の状態を確認
            val currentExpression = _expression.value.trim()
            val currentDisplay = _displayText.value.trim()

            // 1. 式が空の場合
            if (currentExpression.isEmpty()) {
                // マイナス記号のみ許可、他は無視
                if (internalOperator == "-") {
                    _expression.value = "-"
                    _displayText.value = "-"
                    _shouldResetOnNextInput.value = false
                }
                syncPreviewAndMoveCaretEnd()
                return
            }

            // 2. 結果表示状態からの継続
            if (_isResultShowing.value) {
                val baseDisp = if (currentDisplay.isNotEmpty()) currentDisplay else _expression.value.replace('*','×').replace('/','÷')
                val baseExpr = if (currentExpression.isNotEmpty()) currentExpression else _expression.value
                _expression.value = "$baseExpr $internalOperator "
                _displayText.value = "$baseDisp $displayOperator "
                _isResultShowing.value = false
            } else {
                // 3. 末尾が演算子かチェック（連続演算子の防止）
                val lastChar = currentExpression.lastOrNull()
                if (lastChar != null && "+-*/PC".contains(lastChar)) {
                    // 末尾の演算子を新しい演算子に置き換え（displayが空でも安全に処理）
                    val expBase = currentExpression.dropLast(1).trim()
                    val dispBase = if (currentDisplay.isNotEmpty()) {
                        currentDisplay.dropLast(1).trim()
                    } else {
                        // 表示側が空なら内部式ベースを×/÷に置換して利用
                        expBase.replace('*', '×').replace('/', '÷')
                    }
                    _expression.value = "$expBase $internalOperator "
                    _displayText.value = "$dispBase $displayOperator "
                } else {
                    // 4. 通常の演算子追加
                    _expression.value = currentExpression + " $internalOperator "
                    _displayText.value = currentDisplay + " $displayOperator "
                }
            }

            _shouldResetOnNextInput.value = true
            syncPreviewAndMoveCaretEnd()
            
        } catch (e: Exception) {
            Log.e("Calculator", "Critical error in onOperatorClicked: ${e.message}", e)
            // エラー時は安全な状態にリセット
            _displayText.value = "Error"
            _expression.value = ""
            _isResultShowing.value = true
            _shouldResetOnNextInput.value = false
        }
    }

    /**
     * 等号ボタンが押された時の処理
     */
    fun onEqualsClicked() {
        if (isExponentPlaceholderActive) {
            if (_displayText.value.endsWith(exponentPlaceholder)) {
                _displayText.value = _displayText.value.dropLast(exponentPlaceholder.length)
            } else if (_displayText.value.endsWith(superscriptPlaceholder)) {
                _displayText.value = _displayText.value.dropLast(superscriptPlaceholder.length)
            }
            // 内部式の上付きプレースホルダーも削除
            if (_expression.value.endsWith(superscriptPlaceholder)) {
                _expression.value = _expression.value.dropLast(superscriptPlaceholder.length)
            }
            isExponentPlaceholderActive = false
        }
    val safeExpr = _expression.value.replace('×','*').replace('÷','/')
    val result = calculationEngine.evaluate(safeExpr)
    // デバッグ: 評価前の式を出力
    Log.d("Calculator", "Evaluating expression: ${_expression.value}")
        _displayText.value = result
        
        // 計算履歴に保存（エラーでない場合）
        if (!result.contains("Error") && result != "Division by Zero" && result != "Overflow Error") {
            // 前回の結果を保存
            lastResult = result
            
            viewModelScope.launch {
                val entry = CalculationEntry(
                    expression = safeExpr,
                    result = result,
                    timestamp = Date(),
                    calculationType = _calculationType.value
                )
                repository.insertEntry(entry)
            }
        }
        
        _isResultShowing.value = true
        _shouldResetOnNextInput.value = false
        // 結果表示時はプレビューをクリア
        _previewResult.value = ""
        // 入力欄のキャレットを末尾へ
        _expressionField.value = TextFieldValue(
            text = _expression.value,
            selection = androidx.compose.ui.text.TextRange(_expression.value.length)
        )
    }

    /**
     * クリアボタンが押された時の処理
     */
    fun onClearClicked() {
        _displayText.value = ""
        _expression.value = ""
        _isResultShowing.value = false
        _shouldResetOnNextInput.value = false
    // 指数編集中フラグもリセット（AC後の先頭が上付きになるのを防止）
    isExponentPlaceholderActive = false
        // プレビュー結果もクリア
        _previewResult.value = ""
    // 入力欄も完全クリアし、キャレット位置を先頭へ
    _expressionField.value = TextFieldValue("")
    }

    /**
     * バックスペースボタンが押された時の処理
     */
    fun onBackspaceClicked() {
        if (_isResultShowing.value) {
            onClearClicked()
        } else {
            val currentExpression = _expression.value
            val currentDisplay = _displayText.value
            
            if (currentExpression.isNotEmpty()) {
                // 10^x / e^x のペアを同期的に削除して不整合を避ける
                val pairedHandled = when {
                    currentDisplay.endsWith("10^□") && currentExpression.endsWith("10^") -> {
                        _displayText.value = currentDisplay.dropLast(4)
                        _expression.value = currentExpression.dropLast(3)
                        true
                    }
                    currentDisplay.endsWith("e□") && currentExpression.endsWith("e^") -> {
                        _displayText.value = currentDisplay.dropLast(2)
                        _expression.value = currentExpression.dropLast(2)
                        true
                    }
                    // EXP: 表示×10□ と 内部 *10^ を同期削除
                    currentDisplay.endsWith("×10□") && currentExpression.endsWith("*10^") -> {
                        _displayText.value = currentDisplay.dropLast(3)
                        _expression.value = currentExpression.dropLast(4)
                        true
                    }
                    else -> false
                }
                if (!pairedHandled) {
                    // 通常のトークン削除
                    val newExpression = deleteLastToken(currentExpression)
                    val newDisplay = deleteLastToken(currentDisplay)
                    _expression.value = newExpression
                    _displayText.value = newDisplay
                    // EXP/10^/e^ の指数が空に戻ったら表示にプレースホルダーを復帰
                    when {
                        _expression.value.endsWith("*10^") && (_displayText.value.endsWith("×10") || _displayText.value.endsWith("×10^")) && !_displayText.value.endsWith("×10□") -> {
                            _displayText.value = _displayText.value.removeSuffix("^") + "□"
                            isExponentPlaceholderActive = true
                        }
                        _expression.value.endsWith("10^") && _displayText.value.endsWith("10") && !_displayText.value.endsWith("10□") -> {
                            _displayText.value += "□"
                            isExponentPlaceholderActive = true
                        }
                        _expression.value.endsWith("e^") && _displayText.value.endsWith("e") && !_displayText.value.endsWith("e□") -> {
                            _displayText.value += "□"
                            isExponentPlaceholderActive = true
                        }
                    }
                }
        // 指数編集中フラグを再設定：未確定のときだけtrue
        isExponentPlaceholderActive = _expression.value.endsWith("□") ||
            _expression.value.endsWith("10^") || _expression.value.endsWith("e^") || _expression.value.endsWith("*10^")
            } else {
                _displayText.value = ""
                _expression.value = ""
            }
        }
        // プレビュー結果を更新
    syncPreviewAndMoveCaretEnd()
    }
    
    /**
     * 最後のトークンを適切に削除する
     */
    private fun deleteLastToken(text: String): String {
        if (text.isEmpty()) return ""
        
        // 空白付きのオペレーターを識別して削除（例: " + ", " - ", " × ", " ÷ "）
        val spaceOperatorPatterns = listOf(
            " + ", " - ", " × ", " ÷ ", " * ", " / ", " % ", " ^ "
        )
        
        for (pattern in spaceOperatorPatterns) {
            if (text.endsWith(pattern)) {
                return text.dropLast(pattern.length)
            }
        }
        
        // 複数文字のオペレーターや関数を識別して削除
        val multiCharPatterns = listOf(
            "√□", "³√□", "10^", "e^", "sin(", "cos(", "tan(", "log(", "ln(", "π", "e"
        )
        
        for (pattern in multiCharPatterns) {
            if (text.endsWith(pattern)) {
                return text.dropLast(pattern.length)
            }
        }
        
        // 指数の上付きプレースホルダー/マイナスを安全に処理
        if (text.endsWith("⁻□")) {
            // 負号を取り消してプレースホルダーを残す
            return text.dropLast(2) + "□"
        }
        if (text.endsWith("□")) {
            // プレースホルダー自体を削除
            return text.dropLast(1)
        }
        // 上付き数字の削除（最後の桁を消す）。直前が '⁻' のみになるなら '⁻□' に戻す
        val superscriptDigits = "⁰¹²³⁴⁵⁶⁷⁸⁹"
        if (text.isNotEmpty() && superscriptDigits.contains(text.last())) {
            val removed = text.dropLast(1)
            return if (removed.endsWith('⁻')) {
                removed.dropLast(1) + "⁻□"
            } else removed
        }
        // プレーンのプレースホルダー（関数/根号用）
        if (text.endsWith("□")) {
            return text.dropLast(1)
        }
        
        // 通常の文字を削除
        return text.dropLast(1)
    }

    /**
     * 小数点ボタンが押された時の処理
     */
    fun onDecimalClicked() {
        if (isExponentPlaceholderActive && (_displayText.value.endsWith(exponentPlaceholder) || _displayText.value.endsWith(superscriptPlaceholder))) {
            // プレースホルダーを小数点で置換
            if (_displayText.value.endsWith(exponentPlaceholder)) {
                _displayText.value = _displayText.value.dropLast(exponentPlaceholder.length) + "0."
            } else {
                _displayText.value = _displayText.value.dropLast(superscriptPlaceholder.length) + "0."
            }
            _expression.value += "0."
            isExponentPlaceholderActive = false
            syncPreviewAndMoveCaretEnd()
            return
        }
        if (_isResultShowing.value) {
            _displayText.value = "0."
            _expression.value = "0."
            _isResultShowing.value = false
            _shouldResetOnNextInput.value = false
        } else if (_shouldResetOnNextInput.value) {
            // 演算子を押した後の小数点入力
            _displayText.value = "0."
            _expression.value += "0."
            _shouldResetOnNextInput.value = false
        } else if (!_displayText.value.contains(".")) {
            if (_displayText.value.isEmpty()) {
                _displayText.value = "0."
                if (_expression.value.isEmpty()) {
                    _expression.value = "0."
                } else {
                    _expression.value += "0."
                }
            } else {
                _displayText.value += "."
                _expression.value += "."
            }
        }
        // プレビュー結果を更新
    syncPreviewAndMoveCaretEnd()
    }

    /**
     * 計算タイプを変更
     */
    fun setCalculationType(type: CalculationType) {
        _calculationType.value = type
        onClearClicked() // 計算タイプ変更時はクリア
    }

    /**
     * 科学計算関数（sin, cos, tan等）
     */
    fun onScientificFunctionClicked(function: String) {
        when (function) {
            "sin", "cos", "tan", "asin", "acos", "atan" -> {
                // 三角関数
                _expression.value += "$function("
                _shouldResetOnNextInput.value = false
                syncPreviewAndMoveCaretEnd()
            }
            "sinh", "cosh", "tanh", "asinh", "acosh", "atanh" -> {
                // 双曲線関数
                _expression.value += "$function("
                _shouldResetOnNextInput.value = false
                syncPreviewAndMoveCaretEnd()
            }
            "ln", "log" -> {
                // 対数関数
                _expression.value += "$function("
                _shouldResetOnNextInput.value = false
                syncPreviewAndMoveCaretEnd()
            }
            "logbase" -> {
                // 底を指定する対数
                _expression.value += "log("
                _shouldResetOnNextInput.value = false
                syncPreviewAndMoveCaretEnd()
            }
            "10^x" -> {
                // 10のべき乗：プレースホルダーを用意
                _expression.value += "10^"
                isExponentPlaceholderActive = true
                _shouldResetOnNextInput.value = false
                // 入力欄へ即同期（視覚変換で10□として見える）
                run {
                    val newText = _expression.value
                    _expressionField.value = TextFieldValue(
                        text = newText,
                        selection = androidx.compose.ui.text.TextRange(newText.length)
                    )
                }
                syncPreviewAndMoveCaretEnd()
            }
            "e^x" -> {
                // ネイピア数のべき乗：プレースホルダーを用意
                _expression.value += "e^"
                isExponentPlaceholderActive = true
                _shouldResetOnNextInput.value = false
                // 入力欄へ即同期（視覚変換でe□として見える）
                run {
                    val newText = _expression.value
                    _expressionField.value = TextFieldValue(
                        text = newText,
                        selection = androidx.compose.ui.text.TextRange(newText.length)
                    )
                }
                syncPreviewAndMoveCaretEnd()
            }
            "exp" -> {
                // e^x と同等機能
                _expression.value += "e^"
                isExponentPlaceholderActive = true
                _shouldResetOnNextInput.value = false
                // 入力欄へ即同期
                run {
                    val newText = _expression.value
                    _expressionField.value = TextFieldValue(
                        text = newText,
                        selection = androidx.compose.ui.text.TextRange(newText.length)
                    )
                }
                syncPreviewAndMoveCaretEnd()
            }
            "sqrt", "cbrt" -> {
                // 平方根・立方根
                if (function == "sqrt") {
                    // 数字より先に押されたら警告して中断
                    run {
                        val trimmed = _expression.value.trimEnd()
                        val endsWithSupDigit = trimmed.isNotEmpty() && "⁰¹²³⁴⁵⁶⁷⁸⁹".contains(trimmed.last())
                        val endsWithSupToken = trimmed.endsWith("⁻¹") || trimmed.endsWith("⁻³") || trimmed.endsWith('²') || trimmed.endsWith('³')
                        val hasBase = trimmed.isNotEmpty() && (
                            trimmed.last().isDigit() || trimmed.last() == ')' || endsWithSupDigit || endsWithSupToken ||
                            trimmed.last() == 'π' || trimmed.last() == 'e'
                        )
                        if (!hasBase) {
                            _displayText.value = "数字から入力してください"
                            _expression.value = ""
                            _isResultShowing.value = true
                            _shouldResetOnNextInput.value = false
                            syncPreviewAndMoveCaretEnd()
                            return
                        }
                    }
                    // 直前が数値なら、その数値を √ の中に入れる（例: "9" → "√9"）
                    run {
                        val currentExpr = _expression.value
                        val m = Regex("(\\d+(?:\\.\\d*)?)$").find(currentExpr)
                        if (m != null) {
                            val num = m.groupValues[1]
                            val prefix = currentExpr.dropLast(num.length)
                            _expression.value = prefix + "√" + num
                            _displayText.value = formatExpressionForDisplay(_expression.value)
                            // 編集フィールドにも即時反映（キャレットは末尾へ）
                            run {
                                val newText = _expression.value
                                _expressionField.value = TextFieldValue(
                                    text = newText,
                                    selection = androidx.compose.ui.text.TextRange(newText.length)
                                )
                            }
                            try {
                                val result = calculationEngine.evaluate(_expression.value)
                                if (!result.contains("Error") && result != "Division by Zero" &&
                                    result != "Overflow Error" && result != "Math Error") {
                                    _previewResult.value = result
                                } else {
                                    _previewResult.value = ""
                                }
                            } catch (_: Exception) { _previewResult.value = "" }
                            syncPreviewAndMoveCaretEnd()
                            return
                        }
                    }
                    // 数値で終わっていない場合は従来通り √ の入力を開始
                    _expression.value += "√□"
                    _previewResult.value = "√□"
                    syncPreviewAndMoveCaretEnd()
                    return
                } else {
                    // 立方根は従来通り
                    val currentExpr = _expression.value
                    if (currentExpr.isNotEmpty() && currentExpr.matches(Regex("\\d+(\\.\\d+)?$"))) {
                        val currentNumber = currentExpr
                        _expression.value = "³√$currentNumber"
                        try {
                            val result = calculationEngine.evaluate(_expression.value)
                            if (!result.contains("Error") && result != "Division by Zero" &&
                                result != "Overflow Error" && result != "Math Error") {
                                _previewResult.value = result
                            }
                        } catch (_: Exception) { 
                            _previewResult.value = ""
                        }
                    } else {
                        _expression.value += "³√□"
                        _previewResult.value = "³√□"
                        // 早期リターン前にキャレットを末尾へ
                        syncPreviewAndMoveCaretEnd()
                        return
                    }
                }
                _shouldResetOnNextInput.value = false
                syncPreviewAndMoveCaretEnd()
            }
            "^", "^2", "^3", "^-3" -> {
                // べき乗
                if (function == "^2") {
                    // 数字より先に押されたら警告して中断
                    run {
                        val trimmed = _expression.value.trimEnd()
                        val endsWithSupDigit = trimmed.isNotEmpty() && "⁰¹²³⁴⁵⁶⁷⁸⁹".contains(trimmed.last())
                        val endsWithSupToken = trimmed.endsWith("⁻¹") || trimmed.endsWith("⁻³") || trimmed.endsWith('²') || trimmed.endsWith('³')
                        val hasBase = trimmed.isNotEmpty() && (
                            trimmed.last().isDigit() || trimmed.last() == ')' || endsWithSupDigit || endsWithSupToken ||
                            trimmed.last() == 'π' || trimmed.last() == 'e'
                        )
                        if (!hasBase) {
                            _displayText.value = "数字から入力してください"
                            _expression.value = ""
                            _isResultShowing.value = true
                            _shouldResetOnNextInput.value = false
                            syncPreviewAndMoveCaretEnd()
                            return
                        }
                    }
                    try {
                        _expression.value += "^2"
                        _displayText.value = formatExpressionForDisplay(_expression.value)
                        // ^2の場合は即座に計算結果をプレビューに表示
                        val result = calculationEngine.evaluate(_expression.value)
                        if (!result.contains("Error") && result != "Division by Zero" &&
                            result != "Overflow Error" && result != "Math Error") {
                            _previewResult.value = result
                        } else {
                            _previewResult.value = ""
                        }
                    } catch (e: Exception) {
                        // エラーの場合は何もしない
                        _previewResult.value = ""
                    }
                    syncPreviewAndMoveCaretEnd()
                    return
                } else if (function == "^3") {
                    // 数字より先に押されたら警告して中断
                    run {
                        val trimmed = _expression.value.trimEnd()
                        val endsWithSupDigit = trimmed.isNotEmpty() && "⁰¹²³⁴⁵⁶⁷⁸⁹".contains(trimmed.last())
                        val endsWithSupToken = trimmed.endsWith("⁻¹") || trimmed.endsWith("⁻³") || trimmed.endsWith('²') || trimmed.endsWith('³')
                        val hasBase = trimmed.isNotEmpty() && (
                            trimmed.last().isDigit() || trimmed.last() == ')' || endsWithSupDigit || endsWithSupToken ||
                            trimmed.last() == 'π' || trimmed.last() == 'e'
                        )
                        if (!hasBase) {
                            _displayText.value = "数字から入力してください"
                            _expression.value = ""
                            _isResultShowing.value = true
                            _shouldResetOnNextInput.value = false
                            syncPreviewAndMoveCaretEnd()
                            return
                        }
                    }
                    try {
                        _expression.value += "^3"
                        _displayText.value = formatExpressionForDisplay(_expression.value)
                        // ^3の場合は即座に計算結果をプレビューに表示
                        val result = calculationEngine.evaluate(_expression.value)
                        if (!result.contains("Error") && result != "Division by Zero" &&
                            result != "Overflow Error" && result != "Math Error") {
                            _previewResult.value = result
                        } else {
                            _previewResult.value = ""
                        }
                    } catch (e: Exception) {
                        // エラーの場合は何もしない
                        _previewResult.value = ""
                    }
                    syncPreviewAndMoveCaretEnd()
                    return
                } else if (function == "^-3") {
                    // 数字より先に押されたら警告して中断
                    run {
                        val trimmed = _expression.value.trimEnd()
                        val endsWithSupDigit = trimmed.isNotEmpty() && "⁰¹²³⁴⁵⁶⁷⁸⁹".contains(trimmed.last())
                        val endsWithSupToken = trimmed.endsWith("⁻¹") || trimmed.endsWith("⁻³") || trimmed.endsWith('²') || trimmed.endsWith('³')
                        val hasBase = trimmed.isNotEmpty() && (
                            trimmed.last().isDigit() || trimmed.last() == ')' || endsWithSupDigit || endsWithSupToken ||
                            trimmed.last() == 'π' || trimmed.last() == 'e'
                        )
                        if (!hasBase) {
                            _displayText.value = "数字から入力してください"
                            _expression.value = ""
                            _isResultShowing.value = true
                            _shouldResetOnNextInput.value = false
                            syncPreviewAndMoveCaretEnd()
                            return
                        }
                    }
                    try {
                        _expression.value += "^-3"
                        _displayText.value = formatExpressionForDisplay(_expression.value)
                        // ^-3の場合も即座に計算結果をプレビューに表示
                        val result = calculationEngine.evaluate(_expression.value)
                        if (!result.contains("Error") && result != "Division by Zero" &&
                            result != "Overflow Error" && result != "Math Error") {
                            _previewResult.value = result
                        } else {
                            _previewResult.value = ""
                        }
                    } catch (e: Exception) {
                        _previewResult.value = ""
                    }
                    syncPreviewAndMoveCaretEnd()
                    return
                } else {
                    // 一般べき乗：基数が必要
                    val trimmed = _expression.value.trimEnd()
                    val needsBase = trimmed.isEmpty() ||
                            trimmed.endsWith("+") || trimmed.endsWith("-") ||
                            trimmed.endsWith("*") || trimmed.endsWith("/") ||
                            trimmed.endsWith("%") || trimmed.endsWith("P") ||
                            trimmed.endsWith("C") || trimmed.endsWith("^") ||
                            trimmed.endsWith("(") || trimmed.endsWith("√")
                    if (needsBase) {
                        _displayText.value = "数字から入力してください"
                        _expression.value = ""
                        _isResultShowing.value = true
                        _shouldResetOnNextInput.value = false
                        syncPreviewAndMoveCaretEnd()
                        return
                    }
                    // 基数あり → '^' を付与し指数入力を開始（内部式は '^' と通常数字で保持）
                    _expression.value += "^"
                    _displayText.value = formatExpressionForDisplay(_expression.value)
                    // 編集フィールドにも即同期（即座に 9□ を見せるため）
                    run {
                        val newText = _expression.value
                        _expressionField.value = TextFieldValue(
                            text = newText,
                            selection = androidx.compose.ui.text.TextRange(newText.length)
                        )
                    }
                    isExponentPlaceholderActive = true
                }
                _shouldResetOnNextInput.value = false
                syncPreviewAndMoveCaretEnd()
            }
            "!" -> {
                // 階乗: 数字より先に押されたら警告して中断
                val trimmed = _expression.value.trimEnd()
                val endsWithSupDigit = trimmed.isNotEmpty() && "⁰¹²³⁴⁵⁶⁷⁸⁹".contains(trimmed.last())
                val endsWithSupToken = trimmed.endsWith("⁻¹") || trimmed.endsWith("⁻³") || trimmed.endsWith('²') || trimmed.endsWith('³')
                val hasBase = trimmed.isNotEmpty() && (
                    trimmed.last().isDigit() || trimmed.last() == ')' || endsWithSupDigit || endsWithSupToken ||
                    trimmed.last() == 'π' || trimmed.last() == 'e'
                )

                if (!hasBase) {
                    _displayText.value = "数字から入力してください"
                    _expression.value = ""
                    _isResultShowing.value = true
                    _shouldResetOnNextInput.value = false
                    syncPreviewAndMoveCaretEnd()
                    return
                }

                // 基数あり → 階乗を付与
                _expression.value = trimmed + "!"
                _displayText.value = formatExpressionForDisplay(_expression.value)
                // 編集フィールドにも即時反映（キャレットは末尾へ）
                run {
                    val newText = _expression.value
                    _expressionField.value = TextFieldValue(
                        text = newText,
                        selection = androidx.compose.ui.text.TextRange(newText.length)
                    )
                }

                // プレビュー更新（評価できる場合のみ）
                try {
                    val exprNow = _expression.value
                    val result = calculationEngine.evaluate(exprNow)
                    if (!result.contains("Error") && result != "Division by Zero" &&
                        result != "Overflow Error" && result != "Math Error") {
                        _previewResult.value = result
                    } else {
                        _previewResult.value = ""
                    }
                } catch (_: Exception) {
                    _previewResult.value = ""
                }

                _shouldResetOnNextInput.value = false
                syncPreviewAndMoveCaretEnd()
                return
            }
            "+/-" -> {
                // 符号変更
                // X^y の指数入力中なら指数の符号として扱う
                if (isExponentPlaceholderActive) {
                    // 内部式/表示ともに上付きの負号を扱う
                    if (!_displayText.value.contains("⁻□")) {
                        _displayText.value = _displayText.value.replaceFirst("□", "⁻□")
                        _expression.value = _expression.value.replaceFirst("□", "⁻□")
                    } else {
                        _displayText.value = _displayText.value.replaceFirst("⁻□", "□")
                        _expression.value = _expression.value.replaceFirst("⁻□", "□")
                    }
                    syncPreviewAndMoveCaretEnd()
                    return
                }
                if (_isResultShowing.value) {
                    val current = _displayText.value
                    if (current.startsWith("-")) {
                        _displayText.value = current.substring(1)
                        _expression.value = current.substring(1)
                    } else if (current != "0" && current.isNotEmpty()) {
                        _displayText.value = "-$current"
                        _expression.value = "-$current"
                    }
                    _isResultShowing.value = false
                } else {
                    // 負の数の入力を開始
                    if (_shouldResetOnNextInput.value) {
                        _displayText.value = "-"
                        _expression.value += "-"
                        _shouldResetOnNextInput.value = false
                    } else {
                        val current = _displayText.value
                        if (current.startsWith("-")) {
                            _displayText.value = current.substring(1)
                            // 式の最後の部分も更新
                            if (_expression.value.endsWith(current)) {
                                _expression.value = _expression.value.dropLast(current.length) + current.substring(1)
                            }
                        } else if (current.isNotEmpty()) {
                            _displayText.value = "-$current"
                            // 式の最後の部分も更新
                            if (_expression.value.endsWith(current)) {
                                _expression.value = _expression.value.dropLast(current.length) + "-$current"
                            }
                        }
                    }
                }
            }
            "integral" -> {
                // 旧: 積分は削除
            }
            "xroot" -> {
                // 一般根号: ˣ√y を入力開始
                // 表示/内部: □√□（xは上付きプレースホルダー、yは□）
                _expression.value += "□√□"
                isExponentPlaceholderActive = true
                _shouldResetOnNextInput.value = false
                _previewResult.value = "□√□"
                syncPreviewAndMoveCaretEnd()
                return
            }
            "d/dx" -> {
                // 微分（暫定的に実装）
                _expression.value += "d/dx("
                _shouldResetOnNextInput.value = false
                syncPreviewAndMoveCaretEnd()
            }
            "EXP" -> {
                // エンジニアリングEXP: 現在の値に ×10 の指数入力を開始
                // 内部式は *10^、表示は ×10□（上付きプレースホルダ）
                // 直前に基数が必要。先頭や演算子直後で押されたら警告して中断
                val trimmed = _expression.value.trimEnd()
                val needsBase = trimmed.isEmpty() ||
                        trimmed.endsWith("+") || trimmed.endsWith("-") ||
                        trimmed.endsWith("*") || trimmed.endsWith("/") ||
                        trimmed.endsWith("%") || trimmed.endsWith("P") ||
                        trimmed.endsWith("C") || trimmed.endsWith("^") ||
                        trimmed.endsWith("(") || trimmed.endsWith("√")
                if (needsBase) {
                    _displayText.value = "数字から入力してください"
                    _expression.value = ""
                    _isResultShowing.value = true
                    _shouldResetOnNextInput.value = false
                    return
                }
                if (_isResultShowing.value) {
                    _isResultShowing.value = false
                }
                _expression.value += "*10^"
                isExponentPlaceholderActive = true
                _shouldResetOnNextInput.value = false
                // 入力欄へ即同期（視覚変換で×10^ → ×10□として見える）
                run {
                    val newText = _expression.value
                    _expressionField.value = TextFieldValue(
                        text = newText,
                        selection = androidx.compose.ui.text.TextRange(newText.length)
                    )
                }
                syncPreviewAndMoveCaretEnd()
            }
            "ANS" -> {
                // 前回の答え
                if (_isResultShowing.value) {
                    // 結果が表示されている場合は何もしない
                    return
                }
                
                _expression.value += lastResult
                _displayText.value = lastResult
                _shouldResetOnNextInput.value = false
            }
            "STO" -> {
                // メモリに保存
                memoryValue = _displayText.value
            }
            "RCL" -> {
                // メモリから呼び出し
                if (_isResultShowing.value) {
                    _displayText.value = memoryValue
                    _expression.value = memoryValue
                    _isResultShowing.value = false
                } else if (_shouldResetOnNextInput.value) {
                    _displayText.value = memoryValue
                    _expression.value += memoryValue
                    _shouldResetOnNextInput.value = false
                } else {
                    _displayText.value = memoryValue
                    _expression.value += memoryValue
                }
            }
            "M+", "M-" -> {
                // メモリ加算・減算
                val currentValue = try {
                    _displayText.value.toDoubleOrNull() ?: 0.0
                } catch (e: Exception) {
                    0.0
                }
                
                val memValue = try {
                    memoryValue.toDoubleOrNull() ?: 0.0
                } catch (e: Exception) {
                    0.0
                }
                
                memoryValue = if (function == "M+") {
                    (memValue + currentValue).toString()
                } else {
                    (memValue - currentValue).toString()
                }
            }
            "MC" -> {
                // メモリクリア
                memoryValue = "0"
            }
            "MR" -> {
                // メモリ読み出し（RCLと同じ）
                onScientificFunctionClicked("RCL")
            }
            "ENG" -> {
                // エンジニアリング記法（暫定的に実装）
                // 現状では表示のみ
            }
            "S⇔D" -> {
                // 分数⇔小数変換（暫定的に実装）
                // 現状では表示のみ
            }
            "dms" -> {
                // 度分秒変換（暫定的に実装）
                // 現状では表示のみ
            }
            "deg", "rad" -> {
                // 角度単位切り替え（暫定的に実装）
                // 現状では表示のみ
            }
            "1/x" -> {
                // 逆数
                try {
                    // 数字より先に押されたら警告して中断
                    run {
                        val trimmed = _expression.value.trimEnd()
                        val endsWithSupDigit = trimmed.isNotEmpty() && "⁰¹²³⁴⁵⁶⁷⁸⁹".contains(trimmed.last())
                        val endsWithSupToken = trimmed.endsWith("⁻¹") || trimmed.endsWith("⁻³") || trimmed.endsWith('²') || trimmed.endsWith('³')
                        val hasBase = trimmed.isNotEmpty() && (
                            trimmed.last().isDigit() || trimmed.last() == ')' || endsWithSupDigit || endsWithSupToken ||
                            trimmed.last() == 'π' || trimmed.last() == 'e'
                        )
                        if (!hasBase) {
                            _displayText.value = "数字から入力してください"
                            _expression.value = ""
                            _isResultShowing.value = true
                            _shouldResetOnNextInput.value = false
                            syncPreviewAndMoveCaretEnd()
                            return
                        }
                    }
                    _expression.value += "^-1"
                    _displayText.value = formatExpressionForDisplay(_expression.value)
                    // 数字が入力されている場合は即座に計算結果をプレビューに表示
                    val currentExpr = _expression.value
                    if (currentExpr.matches(Regex(".*\\d+\\^-1$"))) {
                        val result = calculationEngine.evaluate(currentExpr)
                        if (!result.contains("Error") && result != "Division by Zero" &&
                            result != "Overflow Error" && result != "Math Error") {
                            _previewResult.value = result
                        } else {
                            _previewResult.value = ""
                        }
                    }
                } catch (e: Exception) {
                    // エラーの場合は何もしない
                    _previewResult.value = ""
                }
                _shouldResetOnNextInput.value = false
                syncPreviewAndMoveCaretEnd()
                return
            }
            "yroot" -> {
                // y乗根（暫定的に実装）
                _expression.value += "^(1/"
                _displayText.value += "^(1/"
                _shouldResetOnNextInput.value = false
            }
        }
        syncPreviewAndMoveCaretEnd()
    }

    /**
     * 進数変換ボタンが押された時の処理
     */
    fun onBaseConversionClicked(base: String) {
        // 現在の数値を取得して進数変換（暫定的に実装）
        val currentValue = _displayText.value
        try {
            val decimalValue = when {
                currentValue.startsWith("0x") -> currentValue.substring(2).toLong(16)
                currentValue.startsWith("0") && currentValue.length > 1 && currentValue.all { it.isDigit() } -> currentValue.toLong(8)
                currentValue.all { it in "01" } -> currentValue.toLong(2)
                else -> currentValue.toLongOrNull() ?: return
            }
            
            val convertedValue = when (base) {
                "HEX" -> "0x${decimalValue.toString(16).uppercase()}"
                "DEC" -> decimalValue.toString()
                "OCT" -> "0${decimalValue.toString(8)}"
                "BIN" -> decimalValue.toString(2)
                else -> currentValue
            }
            
            _displayText.value = convertedValue
            _expression.value = decimalValue.toString() // 内部計算用は10進数で保持
            _shouldResetOnNextInput.value = true
        } catch (e: Exception) {
            // 変換に失敗した場合は何もしない
        }
    }

    /**
     * 履歴をすべてクリア
     */
    fun clearAllHistory() {
        viewModelScope.launch {
            try {
                repository.deleteAllEntries()
                // 削除後にデータベースが更新されるまで少し待つ
                kotlinx.coroutines.delay(100)
            } catch (e: Exception) {
                // エラーログの出力
                e.printStackTrace()
            }
        }
    }

    /**
     * 履歴エントリを削除
     */
    fun deleteHistoryEntry(id: Long) {
        viewModelScope.launch {
            // IDからエントリを取得して削除
            // Note: Room doesn't have a direct delete by ID method in the current DAO,
            // so we need to add that functionality or use a different approach
            // For now, we'll add a delete by ID method to the repository
            repository.deleteEntryById(id)
        }
    }

    /**
     * 括弧ボタンが押された時の処理
     */
    fun onParenthesisClicked(parenthesis: String) {
        if (_isResultShowing.value) {
            // 計算結果が表示されている場合、新しい計算を開始
            if (parenthesis == "(") {
                _displayText.value = "("
                _expression.value = "("
                _shouldResetOnNextInput.value = true
            }
            _isResultShowing.value = false
        } else {
            _expression.value += parenthesis
            if (parenthesis == "(") {
                // 開き括弧の場合、次の数字入力をリセット
                _displayText.value = "("
                _shouldResetOnNextInput.value = true
            } else {
                // 閉じ括弧の場合、現在の表示を維持
                _displayText.value += parenthesis
            }
        }
    // プレビュー結果を更新 + キャレットは末尾へ
    syncPreviewAndMoveCaretEnd()
    }

    /**
     * 括弧の組み合わせボタンが押された時の処理
     * 開き括弧の数に基づいて適切な括弧を挿入
     */
    fun onParenthesesClicked() {
        val currentExpression = _expression.value
        
        // 開き括弧と閉じ括弧の数をカウント
        val openParens = currentExpression.count { it == '(' }
        val closeParens = currentExpression.count { it == ')' }
        
        // 最後の文字を確認
        val lastChar = currentExpression.lastOrNull()
        
        // 適切な括弧を決定
        val shouldAddOpenParen = when {
            // 式が空の場合は開き括弧
            currentExpression.isEmpty() -> true
            // 最後が演算子または開き括弧の場合は開き括弧
            lastChar in setOf('+', '-', '*', '/', '(') -> true
            // 開き括弧が多い場合は閉じ括弧
            openParens > closeParens -> false
            // その他の場合は開き括弧
            else -> true
        }
        
        val parenthesis = if (shouldAddOpenParen) "(" else ")"
        onParenthesisClicked(parenthesis)
    }

    fun onArrowClicked(direction: String) {
        val tv = _expressionField.value
        val text = tv.text
        val sel = tv.selection
        fun setCaret(pos: Int) {
            val p = pos.coerceIn(0, text.length)
            _expressionField.value = tv.copy(selection = androidx.compose.ui.text.TextRange(p))
        }
        suppressMoveCaretOnPreview = true
        when (direction) {
            "LEFT" -> setCaret((sel.min - 1).coerceAtLeast(0))
            "RIGHT" -> setCaret((sel.max + 1).coerceAtMost(text.length))
            "UP" -> setCaret(0) // 単一行なので先頭へ
            "DOWN" -> setCaret(text.length) // 単一行なので末尾へ
        }
        // 位置反映だけ行い、プレビュー更新で末尾強制しない
        updatePreviewResult()
        suppressMoveCaretOnPreview = false
    }
}
