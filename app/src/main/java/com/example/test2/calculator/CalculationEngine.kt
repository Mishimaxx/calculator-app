package com.example.test2.calculator

import kotlin.math.*
import java.math.BigDecimal

/**
 * 計算エンジン - 式の評価と各種計算機能を提供
 */
class CalculationEngine {
    
    // 角度モード（ラジアン/度）
    enum class AngleMode {
        DEGREES, RADIANS
    }
    
    private var angleMode = AngleMode.DEGREES

    /**
     * 基本的な数式を評価
     * @param expression 計算式（例: "2 + 3 * 4"）
     * @return 計算結果、エラーの場合は"Error"
     */
    fun evaluate(expression: String): String {
        return try {
            if (expression.isBlank()) return "Error"
            var processedExpression = expression.replace(" ", "")
            // 上付き数字やプレースホルダーが混入しても評価できるよう正規化
            processedExpression = normalizeSuperscripts(processedExpression)
            
            // 科学記法（E表記）を処理
            processedExpression = processedExpression.replace("E", "*10^")
            
            // 関数を処理
            processedExpression = processFunctions(processedExpression)

            // 未処理の英字（未完の関数呼び出しなど）が残っている場合は評価せず終了
            if (processedExpression.any { it.isLetter() }) {
                return "Error"
            }
            
            val result = evaluateExpression(processedExpression)
            
            // NaN/∞のみエラー扱い（サイズ制限は撤廃）
            if (result.isNaN() || result.isInfinite()) {
                "Math Error"
            } else {
                formatResult(result)
            }
        } catch (e: ArithmeticException) {
            "Division by Zero"
        } catch (e: Exception) {
            // デバッグ用：具体的なエラーを出力
            "Error: ${e.message}"
        }
    }

    /**
     * 科学表記(E表記)を避け、プレーンな10進数文字列に整形する。
     * また末尾の不要な小数点ゼロは除去する。
     */
    private fun toPlainNumberString(value: Double): String {
        // Double.toString() は丸め誤差を含まない10進文字列を返すので、それをBigDecimal化してplainにする
        val bd = try {
            BigDecimal(value.toString())
        } catch (_: Exception) {
            // フォールバック
            BigDecimal.valueOf(value)
        }
        val plain = bd.stripTrailingZeros().toPlainString()
        return if (plain == "-0") "0" else plain
    }

    /**
     * 表示用の上付き指数（例: 9⁹, 10¹², e¹²）をエンジン用の表記 (9^9, 10^12, e^12) に正規化。
     * また、上付きプレースホルダー（▫）は除去する。
     */
    private fun normalizeSuperscripts(expr: String): String {
        if (expr.isEmpty()) return expr

        // 先にプレースホルダーを除去
        var res = expr.replace("▫", "")

        // パターン: <base>(π/e/数字/閉じ括弧) + (上付きの符号付き整数) を <base>^<整数> に正規化
        // 例: 10¹² -> 10^12, e⁻³ -> e^-3, (2+3)² -> (2+3)^2
        val supDigits = "⁰¹²³⁴⁵⁶⁷⁸⁹"
        val supToNormal = mapOf(
            '⁰' to '0','¹' to '1','²' to '2','³' to '3','⁴' to '4',
            '⁵' to '5','⁶' to '6','⁷' to '7','⁸' to '8','⁹' to '9'
        )
    val pattern = Regex("([0-9)πe]+)([⁻]?[$supDigits]+)")
        res = res.replace(pattern) { m ->
            val base = m.groupValues[1]
            val sup = m.groupValues[2]
            // 直後が√のケース（³√など）は除外されるように、ここではマッチしない構造だが念のためチェック
            if (sup == "³" && res.indexOf(m.value) + m.value.length < res.length && res[res.indexOf(m.value) + m.value.length] == '√') {
                m.value // そのまま
            } else {
                val normal = buildString {
                    for (ch in sup) {
                        append(
                            when (ch) {
                                '⁻' -> '-'
                                else -> supToNormal[ch] ?: ch
                            }
                        )
                    }
                }
                "$base^$normal"
            }
        }

    // 孤立した上付き（基数無し）については安全のため通常数字や^記法へ
    // ただし「²√」「³√」のように直後が根号の場合は変換しない（n√yの指数として扱う）
    res = res.replace(Regex("²(?!√)")) { "^2" }
    res = res.replace(Regex("³(?!√)")) { "^3" }
        res = res.replace("⁻¹", "^-1").replace("⁻³", "^-3")
        res = res.replace('⁰', '0').replace('¹', '1').replace('⁴', '4')
            .replace('⁵', '5').replace('⁶', '6').replace('⁷', '7').replace('⁸', '8').replace('⁹', '9')

        return res
    }
    
    /**
     * 関数呼び出しを処理
     */
    private fun processFunctions(expression: String): String {
        var result = expression
        
        // 定数の置換（単独のπとeのみ対象）
        result = result.replace("π", Math.PI.toString())
        // 'e' は英字に挟まれていない単独文字を定数として扱う
        result = result.replace(Regex("(?<![A-Za-z])e(?![A-Za-z])"), Math.E.toString())
        
        // 関数のパターンを処理
        val functionPatterns = listOf(
            // 双曲線関数（逆関数を先に処理）
            "asinh" to ::asinh,
            "acosh" to ::acosh,
            "atanh" to ::atanh,
            "sinh" to ::sinh,
            "cosh" to ::cosh,
            "tanh" to ::tanh,
            
            // 逆三角関数
            "asin" to ::asin,
            "acos" to ::acos,
            "atan" to ::atan,
            
            // 三角関数
            "sin" to ::sin,
            "cos" to ::cos,
            "tan" to ::tan,
            
            // 対数関数
            "ln" to ::ln,
            "log" to ::log,
            
            // 平方根・立方根 - 記号形式も追加
            "cbrt" to ::cbrt,
            "sqrt" to ::sqrt,
            
            // 指数関数
            "exp" to ::exp
        )
        
        for ((funcName, func) in functionPatterns) {
            result = processFunction(result, funcName, func)
        }
        
        // 階乗の処理
        result = processFactorial(result)
        
        // 平方根・立方根記号の処理
        result = processSquareRoot(result)
        
        // 順列・組み合わせの処理
        result = processPermutationCombination(result)
        
        // べき乗の処理
        result = processPower(result)
        
        return result
    }
    
    private fun processFunction(expression: String, funcName: String, func: (Double) -> Double): String {
        var result = expression
        val pattern = "$funcName\\(([^)]+)\\)".toRegex()
        
        while (pattern.containsMatchIn(result)) {
            val match = pattern.find(result)!!
            val argument = match.groupValues[1]
            val argValue = evaluateExpression(argument)
            val funcResult = func(argValue)
            result = result.replace(match.value, toPlainNumberString(funcResult))
        }
        
        return result
    }
    
    private fun processFactorial(expression: String): String {
        var result = expression
        val pattern = "(\\d+)!".toRegex()
        
        while (pattern.containsMatchIn(result)) {
            val match = pattern.find(result)!!
            val number = match.groupValues[1].toInt()
            val factorialResult = factorial(number)
            result = result.replace(match.value, factorialResult.toString())
        }
        
        return result
    }
    
    private fun processSquareRoot(expression: String): String {
        var result = expression
        
        // 一般の n√y パターン: 数字(インデックス) + √ + 数字
        // 例: 4√16 -> 2, 5√32 -> 2
        val nRootPattern = "(\\d+)√([\\d.]+)".toRegex()
        while (nRootPattern.containsMatchIn(result)) {
            val match = nRootPattern.find(result)!!
            val n = match.groupValues[1].toInt()
            val number = match.groupValues[2].toDouble()
            val value = try {
                if (n <= 0) Double.NaN else number.pow(1.0 / n)
            } catch (_: Exception) { Double.NaN }
            result = result.replace(match.value, toPlainNumberString(value))
        }
        
        // √ パターン: √ + 数字
        val sqrtPattern = "√([\\d.]+)".toRegex()
        while (sqrtPattern.containsMatchIn(result)) {
            val match = sqrtPattern.find(result)!!
            val number = match.groupValues[1].toDouble()
            val sqrtResult = sqrt(number)
            result = result.replace(match.value, toPlainNumberString(sqrtResult))
        }
        
        // ³√ パターン: ³√ + 数字  
        val cbrtPattern = "³√([\\d.]+)".toRegex()
        while (cbrtPattern.containsMatchIn(result)) {
            val match = cbrtPattern.find(result)!!
            val number = match.groupValues[1].toDouble()
            val cbrtResult = cbrt(number)
            result = result.replace(match.value, toPlainNumberString(cbrtResult))
        }
        
        return result
    }

    private fun processPermutationCombination(expression: String): String {
        var result = expression
        
        // P パターン: 数字 + P + 数字
        val nPrPattern = "(\\d+)P(\\d+)".toRegex()
        while (nPrPattern.containsMatchIn(result)) {
            val match = nPrPattern.find(result)!!
            val n = match.groupValues[1].toInt()
            val r = match.groupValues[2].toInt()
            val permutationResult = permutation(n, r)
            result = result.replace(match.value, permutationResult.toString())
        }
        
        // C パターン: 数字 + C + 数字
        val nCrPattern = "(\\d+)C(\\d+)".toRegex()
        while (nCrPattern.containsMatchIn(result)) {
            val match = nCrPattern.find(result)!!
            val n = match.groupValues[1].toInt()
            val r = match.groupValues[2].toInt()
            val combinationResult = combination(n, r)
            result = result.replace(match.value, combinationResult.toString())
        }
        
        return result
    }
    
    private fun processPower(expression: String): String {
        var result = expression
        
        // べき乗を簡単に処理
    // 負の指数にも対応
    val powerPattern = Regex("([0-9]+(?:\\.[0-9]+)?)\\^(-?[0-9]+(?:\\.[0-9]+)?)")
        
        while (powerPattern.containsMatchIn(result)) {
            val match = powerPattern.find(result)!!
            val base = match.groupValues[1].toDouble()
            val exponent = match.groupValues[2].toDouble()
            
            val powerResult = base.pow(exponent)
            result = result.replace(match.value, toPlainNumberString(powerResult))
        }
        
        return result
    }

    /**
     * 科学計算関数
     */
    fun sin(value: Double): Double = kotlin.math.sin(toRadians(value))
    fun cos(value: Double): Double = kotlin.math.cos(toRadians(value))
    fun tan(value: Double): Double = kotlin.math.tan(toRadians(value))
    
    // 逆三角は入力を[-1,1]にクランプして丸め誤差起因のNaNを回避
    fun asin(value: Double): Double = fromRadians(kotlin.math.asin(value.coerceIn(-1.0, 1.0)))
    fun acos(value: Double): Double = fromRadians(kotlin.math.acos(value.coerceIn(-1.0, 1.0)))
    fun atan(value: Double): Double = fromRadians(kotlin.math.atan(value))
    
    fun sinh(value: Double): Double = kotlin.math.sinh(value)
    fun cosh(value: Double): Double = kotlin.math.cosh(value)
    fun tanh(value: Double): Double = kotlin.math.tanh(value)
    
    fun asinh(value: Double): Double = kotlin.math.asinh(value)
    fun acosh(value: Double): Double = kotlin.math.acosh(value)
    fun atanh(value: Double): Double = kotlin.math.atanh(value)
    
    fun log(value: Double): Double = log10(value)
    fun ln(value: Double): Double = kotlin.math.ln(value)
    fun exp(value: Double): Double = kotlin.math.exp(value)
    
    fun sqrt(value: Double): Double = kotlin.math.sqrt(value)
    fun cbrt(value: Double): Double = kotlin.math.cbrt(value)
    
    fun power(base: Double, exponent: Double): Double = base.pow(exponent)
    
    fun factorial(n: Int): Long {
        if (n < 0) throw IllegalArgumentException("Factorial is not defined for negative numbers")
        if (n > 20) throw IllegalArgumentException("Factorial too large")
        
        var result = 1L
        for (i in 2..n) {
            result *= i
        }
        return result
    }
    
    /**
     * 順列計算 nPr = n!/(n-r)!
     */
    fun permutation(n: Int, r: Int): Long {
        if (n < 0 || r < 0) throw IllegalArgumentException("n and r must be non-negative")
        if (r > n) throw IllegalArgumentException("r cannot be greater than n")
        if (n > 20) throw IllegalArgumentException("n too large")
        
        var result = 1L
        for (i in (n - r + 1)..n) {
            result *= i
        }
        return result
    }
    
    /**
     * 組み合わせ計算 nCr = n!/(r!(n-r)!)
     */
    fun combination(n: Int, r: Int): Long {
        if (n < 0 || r < 0) throw IllegalArgumentException("n and r must be non-negative")
        if (r > n) throw IllegalArgumentException("r cannot be greater than n")
        if (n > 20) throw IllegalArgumentException("n too large")
        
        // r > n/2 の場合は r = n - r として計算量を削減
        val actualR = if (r > n - r) n - r else r
        
        var result = 1L
        for (i in 1..actualR) {
            result = result * (n - i + 1) / i
        }
        return result
    }
    
    /**
     * 角度変換ヘルパー関数
     */
    private fun toRadians(degrees: Double): Double {
        return if (angleMode == AngleMode.DEGREES) {
            Math.toRadians(degrees)
        } else {
            degrees
        }
    }
    
    private fun fromRadians(radians: Double): Double {
        return if (angleMode == AngleMode.DEGREES) {
            Math.toDegrees(radians)
        } else {
            radians
        }
    }
    
    /**
     * 角度モードを設定
     */
    fun setAngleMode(mode: AngleMode) {
        angleMode = mode
    }

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
        // ほぼ整数の値は整数にスナップ（例: 30.000000000000004 → 30）
        val nearest = result.roundToLong().toDouble()
        if (abs(result - nearest) < 1e-12) {
            return toPlainNumberString(nearest)
        }
        // 極小値は0に丸め
        if (abs(result) < 1e-12) return "0"
        return toPlainNumberString(result)
    }
}
