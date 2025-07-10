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
        val result = calculationEngine.evaluate(_expression.value)
        _displayText.value = result
        
        // 計算履歴に保存（エラーでない場合）
        if (!result.contains("Error") && result != "Division by Zero" && result != "Overflow Error") {
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
        try {
            val currentValue = _displayText.value.toDouble()
            val result = when (function) {
                "sin" -> calculationEngine.sin(currentValue)
                "cos" -> calculationEngine.cos(currentValue)
                "tan" -> calculationEngine.tan(currentValue)
                "log" -> calculationEngine.log(currentValue)
                "ln" -> calculationEngine.ln(currentValue)
                "sqrt" -> calculationEngine.sqrt(currentValue)
                else -> currentValue
            }
            
            val formattedResult = when {
                result.isNaN() || result.isInfinite() -> "Math Error"
                else -> String.format("%.10g", result)
            }
            
            _displayText.value = formattedResult
            _expression.value = "$function($currentValue)"
            _isResultShowing.value = true
            
        } catch (e: Exception) {
            _displayText.value = "Error"
        }
    }

    /**
     * 進数変換
     */
    fun onBaseConversionClicked(targetBase: String) {
        try {
            val currentValue = _displayText.value.toLong()
            val result = when (targetBase) {
                "BIN" -> calculationEngine.toBinary(currentValue)
                "HEX" -> calculationEngine.toHex(currentValue)
                "OCT" -> calculationEngine.toOctal(currentValue)
                else -> currentValue.toString()
            }
            
            _displayText.value = result
            _expression.value = "DEC($currentValue) → $targetBase"
            _isResultShowing.value = true
            
        } catch (e: Exception) {
            _displayText.value = "Error"
        }
    }

    /**
     * 履歴エントリを削除
     */
    fun deleteHistoryEntry(entry: CalculationEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
        }
    }

    /**
     * 全履歴を削除
     */
    fun clearAllHistory() {
        viewModelScope.launch {
            repository.deleteAllEntries()
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
