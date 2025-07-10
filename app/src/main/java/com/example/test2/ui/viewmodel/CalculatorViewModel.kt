package com.example.test2.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
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

    /**
     * 表示用に式を変換する（*→×、/→÷）
     */
    private fun formatExpressionForDisplay(expression: String): String {
        return expression
            .replace("*", "×")
            .replace("/", "÷")
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

    /**
     * 式全体をフォーマットする（演算子変換と数字の3桁区切り）
     */
    private fun formatCompleteExpression(expression: String): String {
        val operatorConverted = expression
            .replace("*", "×")
            .replace("/", "÷")
        
        // 数字部分のみを3桁区切りにフォーマット
        return operatorConverted.replace(Regex("\\d+(?:\\.\\d+)?")) { matchResult ->
            formatNumberWithCommas(matchResult.value)
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
        get() = formatNumberWithCommas(_previewResult.value)

    /**
     * 式のプレビュー結果を更新する
     */
    private fun updatePreviewResult() {
        val expr = _expression.value
        
        if (expr.isNotEmpty() && !_isResultShowing.value) {
            try {            // 演算子を含む完全な式の場合（例：「33+22」）
            if ((expr.contains('+') || expr.contains('-') || expr.contains('*') ||
                 expr.contains('/') || expr.contains('%')) &&
                !expr.trim().endsWith('+') && !expr.trim().endsWith('-') &&
                !expr.trim().endsWith('*') && !expr.trim().endsWith('/') &&
                !expr.trim().endsWith('%')) {
                val result = calculationEngine.evaluate(expr)
                if (!result.contains("Error") && result != "Division by Zero" &&
                    result != "Overflow Error" && result != "Math Error") {
                    _previewResult.value = result
                } else {
                    _previewResult.value = "" // 3÷0などのエラー時は何も表示しない
                }
            }
                // 演算子で終わっている場合（例：「33+」）は演算子の前の数値を表示
                else if (expr.trim().endsWith('+') || expr.trim().endsWith('-') || 
                         expr.trim().endsWith('*') || expr.trim().endsWith('/') || 
                         expr.trim().endsWith('%')) {
                    // 演算子の前の数値を取得
                    val trimmedExpr = expr.trim()
                    val lastOperatorIndex = maxOf(
                        trimmedExpr.lastIndexOf('+'),
                        trimmedExpr.lastIndexOf('-'),
                        trimmedExpr.lastIndexOf('*'),
                        trimmedExpr.lastIndexOf('/'),
                        trimmedExpr.lastIndexOf('%')
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
                        val numberPart = trimmedExpr.dropLast(1).trim()
                        if (numberPart.isNotEmpty()) {
                            _previewResult.value = numberPart
                        } else {
                            _previewResult.value = ""
                        }
                    }
                }
                // 数字のみの場合は表示しない
                else {
                    _previewResult.value = ""
                }
            } catch (e: Exception) {
                _previewResult.value = ""
            }
        } else {
            _previewResult.value = ""
        }
    }

    /**
     * 数字ボタンが押された時の処理
     */
    fun onNumberClicked(number: String) {
        if (isExponentPlaceholderActive && _displayText.value.endsWith(exponentPlaceholder)) {
            // プレースホルダーを数字で置き換える
            _displayText.value = _displayText.value.dropLast(exponentPlaceholder.length) + number
            _expression.value += number
            isExponentPlaceholderActive = false
            // プレビュー結果を更新
            updatePreviewResult()
            return
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
        updatePreviewResult()
    }

    /**
     * 演算子ボタンが押された時の処理
     */
    fun onOperatorClicked(operator: String) {
        if (isExponentPlaceholderActive) {
            // プレースホルダーが残っている場合は削除
            if (_displayText.value.endsWith(exponentPlaceholder)) {
                _displayText.value = _displayText.value.dropLast(exponentPlaceholder.length)
            }
            isExponentPlaceholderActive = false
        }
        if (_isResultShowing.value) {
            // 計算結果が表示されている場合、その結果を使って新しい式を開始
            _expression.value = _displayText.value + " $operator "
            _isResultShowing.value = false
        } else {
            // 現在入力中の数値がある場合、それを式に追加
            if (_displayText.value.isNotEmpty() && _displayText.value != "0" && _expression.value.isEmpty()) {
                _expression.value = _displayText.value + " $operator "
            } else {
                _expression.value += " $operator "
            }
        }
        // 演算子を押した後は新しい数値の入力を待つ状態にする
        _shouldResetOnNextInput.value = true
        // プレビュー結果を更新
        updatePreviewResult()
    }

    /**
     * 等号ボタンが押された時の処理
     */
    fun onEqualsClicked() {
        if (isExponentPlaceholderActive) {
            if (_displayText.value.endsWith(exponentPlaceholder)) {
                _displayText.value = _displayText.value.dropLast(exponentPlaceholder.length)
            }
            isExponentPlaceholderActive = false
        }
        val result = calculationEngine.evaluate(_expression.value)
        _displayText.value = result
        
        // 計算履歴に保存（エラーでない場合）
        if (!result.contains("Error") && result != "Division by Zero" && result != "Overflow Error") {
            // 前回の結果を保存
            lastResult = result
            
            viewModelScope.launch {
                val entry = CalculationEntry(
                    expression = _expression.value,
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
    }

    /**
     * クリアボタンが押された時の処理
     */
    fun onClearClicked() {
        _displayText.value = ""
        _expression.value = ""
        _isResultShowing.value = false
        _shouldResetOnNextInput.value = false
        // プレビュー結果もクリア
        _previewResult.value = ""
    }

    /**
     * バックスペースボタンが押された時の処理
     */
    fun onBackspaceClicked() {
        if (_isResultShowing.value) {
            onClearClicked()
        } else {
            if (_displayText.value.length > 1) {
                _displayText.value = _displayText.value.dropLast(1)
                _expression.value = _expression.value.dropLast(1)
            } else {
                _displayText.value = ""
                _expression.value = ""
            }
        }
        // プレビュー結果を更新
        updatePreviewResult()
    }

    /**
     * 小数点ボタンが押された時の処理
     */
    fun onDecimalClicked() {
        if (isExponentPlaceholderActive && _displayText.value.endsWith(exponentPlaceholder)) {
            // プレースホルダーを小数点で置換
            _displayText.value = _displayText.value.dropLast(exponentPlaceholder.length) + "0."
            _expression.value += "0."
            isExponentPlaceholderActive = false
            updatePreviewResult()
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
        updatePreviewResult()
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
                _displayText.value += "$function("
                _shouldResetOnNextInput.value = false
            }
            "sinh", "cosh", "tanh", "asinh", "acosh", "atanh" -> {
                // 双曲線関数
                _expression.value += "$function("
                _displayText.value += "$function("
                _shouldResetOnNextInput.value = false
            }
            "ln", "log" -> {
                // 対数関数
                _expression.value += "$function("
                _displayText.value += "$function("
                _shouldResetOnNextInput.value = false
            }
            "logbase" -> {
                // 底を指定する対数
                _expression.value += "log("
                _displayText.value += "log("
                _shouldResetOnNextInput.value = false
            }
            "10^x" -> {
                // 10のべき乗：プレースホルダーを用意
                _expression.value += "10^"
                _displayText.value += "10^$exponentPlaceholder"
                isExponentPlaceholderActive = true
                _shouldResetOnNextInput.value = false
            }
            "e^x" -> {
                // ネイピア数のべき乗：プレースホルダーを用意
                _expression.value += "e^"
                _displayText.value += "e^$exponentPlaceholder"
                isExponentPlaceholderActive = true
                _shouldResetOnNextInput.value = false
            }
            "exp" -> {
                // e^x と同等機能
                _expression.value += "e^"
                _displayText.value += "e^$exponentPlaceholder"
                isExponentPlaceholderActive = true
                _shouldResetOnNextInput.value = false
            }
            "sqrt", "cbrt" -> {
                // 平方根・立方根
                if (function == "sqrt") {
                    _expression.value += "sqrt("
                    _displayText.value += "√("
                } else {
                    _expression.value += "cbrt("
                    _displayText.value += "³√("
                }
                _shouldResetOnNextInput.value = false
            }
            "^", "^2", "^3" -> {
                // べき乗
                if (function == "^2") {
                    _expression.value += "^2"
                    _displayText.value += "²"
                } else if (function == "^3") {
                    _expression.value += "^3"
                    _displayText.value += "³"
                } else {
                    // 一般べき乗：プレースホルダーを表示
                    _expression.value += "^"
                    _displayText.value += "^$exponentPlaceholder"
                    isExponentPlaceholderActive = true
                }
                _shouldResetOnNextInput.value = false
            }
            "!" -> {
                // 階乗
                _expression.value += "!"
                _displayText.value += "!"
                _shouldResetOnNextInput.value = false
            }
            "+/-" -> {
                // 符号変更
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
                // 積分（暫定的に実装）
                _expression.value += "∫("
                _displayText.value += "∫("
                _shouldResetOnNextInput.value = false
            }
            "d/dx" -> {
                // 微分（暫定的に実装）
                _expression.value += "d/dx("
                _displayText.value += "d/dx("
                _shouldResetOnNextInput.value = false
            }
            "nPr", "nCr" -> {
                // 順列・組み合わせ（暫定的に実装）
                _expression.value += "$function("
                _displayText.value += "$function("
                _shouldResetOnNextInput.value = false
            }
            "EXP" -> {
                // ×10^x（指数入力）
                _expression.value += "E"
                _displayText.value += "×10^"
                _shouldResetOnNextInput.value = false
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
                _expression.value += "1/("
                _displayText.value += "1/("
                _shouldResetOnNextInput.value = false
            }
            "yroot" -> {
                // y乗根（暫定的に実装）
                _expression.value += "^(1/"
                _displayText.value += "^(1/"
                _shouldResetOnNextInput.value = false
            }
        }
        updatePreviewResult()
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
        // 履歴機能（暫定的に実装）
        // 現状では何もしない
    }

    /**
     * 履歴エントリを削除
     */
    fun deleteHistoryEntry(id: Long) {
        // 履歴機能（暫定的に実装）
        // 現状では何もしない
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
        // プレビュー結果を更新
        updatePreviewResult()
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
}
