package com.example.test2.calculator

import kotlin.math.*

/**
 * 計算エンジン - 式の評価と各種計算機能を提供
 */
class CalculationEngine {

    /**
     * 基本的な数式を評価
     * @param expression 計算式（例: "2 + 3 * 4"）
     * @return 計算結果、エラーの場合は"Error"
     */
    fun evaluate(expression: String): String {
        return try {
            val result = evaluateExpression(expression.replace(" ", ""))
            
            // 結果が非常に大きい場合（10^10以上）のチェック
            if (abs(result) >= 1e10) {
                "Overflow Error"
            } else if (result.isNaN() || result.isInfinite()) {
                "Math Error"
            } else {
                formatResult(result)
            }
        } catch (e: ArithmeticException) {
            "Division by Zero"
        } catch (e: Exception) {
            "Error"
        }
    }

    /**
     * 科学計算関数
     */
    fun sin(value: Double): Double = kotlin.math.sin(Math.toRadians(value))
    fun cos(value: Double): Double = kotlin.math.cos(Math.toRadians(value))
    fun tan(value: Double): Double = kotlin.math.tan(Math.toRadians(value))
    fun log(value: Double): Double = log10(value)
    fun ln(value: Double): Double = kotlin.math.ln(value)
    fun sqrt(value: Double): Double = kotlin.math.sqrt(value)
    fun power(base: Double, exponent: Double): Double = base.pow(exponent)

    /**
     * プログラマ向け関数
     */
    fun toBinary(value: Long): String = value.toString(2)
    fun toHex(value: Long): String = value.toString(16).uppercase()
    fun toOctal(value: Long): String = value.toString(8)
    
    fun fromBinary(binary: String): Long = binary.toLong(2)
    fun fromHex(hex: String): Long = hex.toLong(16)
    fun fromOctal(octal: String): Long = octal.toLong(8)

    // ビット演算
    fun bitwiseAnd(a: Long, b: Long): Long = a and b
    fun bitwiseOr(a: Long, b: Long): Long = a or b
    fun bitwiseXor(a: Long, b: Long): Long = a xor b
    fun bitwiseNot(a: Long): Long = a.inv()
    fun leftShift(a: Long, bits: Int): Long = a shl bits
    fun rightShift(a: Long, bits: Int): Long = a shr bits

    /**
     * 式の評価（再帰下降パーサー）
     */
    private fun evaluateExpression(expression: String): Double {
        if (expression.isEmpty()) {
            throw IllegalArgumentException("Empty expression")
        }
        val tokens = tokenize(expression)
        if (tokens.isEmpty()) {
            throw IllegalArgumentException("Empty expression")
        }
        
        // 括弧の対応をチェック
        var openParens = 0
        for (token in tokens) {
            when (token) {
                "(" -> openParens++
                ")" -> {
                    openParens--
                    if (openParens < 0) {
                        throw IllegalArgumentException("Mismatched parentheses")
                    }
                }
            }
        }
        if (openParens != 0) {
            throw IllegalArgumentException("Mismatched parentheses")
        }
        
        val parser = ExpressionParser(tokens)
        val result = parser.parseExpression()
        
        // すべてのトークンが消費されたかチェック
        if (parser.index < tokens.size) {
            throw IllegalArgumentException("Unexpected token: ${tokens[parser.index]}")
        }
        
        return result
    }

    private class ExpressionParser(private val tokens: List<String>) {
        var index = 0

        fun parseExpression(): Double {
            var result = parseTerm()
            
            while (index < tokens.size) {
                when (tokens[index]) {
                    "+" -> {
                        index++
                        result += parseTerm()
                    }
                    "-" -> {
                        index++
                        result -= parseTerm()
                    }
                    else -> break
                }
            }
            return result
        }

        private fun parseTerm(): Double {
            var result = parseFactor()
            
            while (index < tokens.size) {
                when (tokens[index]) {
                    "*" -> {
                        index++
                        result *= parseFactor()
                    }
                    "/" -> {
                        index++
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Division by zero")
                        result /= divisor
                    }
                    else -> break
                }
            }
            return result
        }

        private fun parseFactor(): Double {
            if (index >= tokens.size) throw IllegalArgumentException("Unexpected end of expression")

            val token = tokens[index++]
            
            return when {
                token == "(" -> {
                    // 空の括弧をチェック
                    if (index < tokens.size && tokens[index] == ")") {
                        throw IllegalArgumentException("Empty parentheses")
                    }
                    val result = parseExpression()
                    if (index >= tokens.size || tokens[index] != ")") {
                        throw IllegalArgumentException("Missing closing parenthesis")
                    }
                    index++ // skip ")"
                    result
                }
                token == "-" -> -parseFactor()
                token == "+" -> parseFactor()
                else -> token.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number: $token")
            }
        }
    }

    /**
     * 式をトークンに分割
     */
    private fun tokenize(expression: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        
        while (i < expression.length) {
            when (val char = expression[i]) {
                in '0'..'9', '.' -> {
                    val start = i
                    while (i < expression.length && (expression[i].isDigit() || expression[i] == '.')) {
                        i++
                    }
                    tokens.add(expression.substring(start, i))
                }
                '+', '-', '*', '/', '(', ')' -> {
                    tokens.add(char.toString())
                    i++
                }
                ' ' -> i++ // スペースをスキップ
                else -> throw IllegalArgumentException("Invalid character: $char")
            }
        }
        
        return tokens
    }

    /**
     * 結果をフォーマット
     */
    private fun formatResult(result: Double): String {
        return when {
            result == result.toLong().toDouble() -> result.toLong().toString()
            abs(result) < 1e-10 -> "0"
            else -> String.format("%.10g", result)
        }
    }
}
