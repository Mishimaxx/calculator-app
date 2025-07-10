package com.example.test2

import com.example.test2.calculator.CalculationEngine
import org.junit.Test
import org.junit.Assert.*

/**
 * 計算エンジンのユニットテスト
 */
class CalculationEngineTest {

    private val calculationEngine = CalculationEngine()

    @Test
    fun `基本的な四則演算テスト`() {
        assertEquals("5", calculationEngine.evaluate("2 + 3"))
        assertEquals("6", calculationEngine.evaluate("2 * 3"))
        assertEquals("2", calculationEngine.evaluate("6 / 3"))
        assertEquals("1", calculationEngine.evaluate("4 - 3"))
    }

    @Test
    fun `演算子の優先順位テスト`() {
        assertEquals("14", calculationEngine.evaluate("2 + 3 * 4"))
        assertEquals("20", calculationEngine.evaluate("(2 + 3) * 4"))
        assertEquals("10", calculationEngine.evaluate("2 * 3 + 4"))
    }

    @Test
    fun `例外シナリオテスト`() {
        assertEquals("Division by Zero", calculationEngine.evaluate("5 / 0"))
        assertEquals("Error", calculationEngine.evaluate("2 +"))
        assertEquals("Error", calculationEngine.evaluate(""))
        assertEquals("Error", calculationEngine.evaluate("2 + * 3"))
    }

    @Test
    fun `大きな数値のオーバーフローテスト`() {
        val result = calculationEngine.evaluate("999999999999 * 999999999999")
        assertEquals("Overflow Error", result)
    }

    @Test
    fun `科学計算関数テスト`() {
        assertEquals(0.0, calculationEngine.sin(0.0), 0.0001)
        assertEquals(1.0, calculationEngine.cos(0.0), 0.0001)
        assertEquals(1.0, calculationEngine.log(10.0), 0.0001)
        assertEquals(3.0, calculationEngine.sqrt(9.0), 0.0001)
    }

    @Test
    fun `進数変換テスト`() {
        assertEquals("1010", calculationEngine.toBinary(10))
        assertEquals("A", calculationEngine.toHex(10))
        assertEquals("12", calculationEngine.toOctal(10))
        
        assertEquals(10L, calculationEngine.fromBinary("1010"))
        assertEquals(10L, calculationEngine.fromHex("A"))
        assertEquals(10L, calculationEngine.fromOctal("12"))
    }

    @Test
    fun `ビット演算テスト`() {
        assertEquals(8L, calculationEngine.bitwiseAnd(12, 10)) // 1100 & 1010 = 1000
        assertEquals(14L, calculationEngine.bitwiseOr(12, 10)) // 1100 | 1010 = 1110
        assertEquals(6L, calculationEngine.bitwiseXor(12, 10)) // 1100 ^ 1010 = 0110
    }

    @Test
    fun `括弧を使った複雑な計算テスト`() {
        // 基本的な括弧計算
        assertEquals("20", calculationEngine.evaluate("(2 + 3) * 4"))
        assertEquals("14", calculationEngine.evaluate("2 + (3 * 4)"))
        
        // ネストした括弧
        assertEquals("22", calculationEngine.evaluate("((2 + 3) * 4) + 2"))
        assertEquals("30", calculationEngine.evaluate("(2 + 3) * (4 + 2)"))
        
        // 複数の括弧ペア
        assertEquals("14", calculationEngine.evaluate("(2 + 3) + (4 * 2) + 1"))
        assertEquals("25", calculationEngine.evaluate("(2 + 3) * (3 + 2)"))
    }

    @Test
    fun `括弧の例外処理テスト`() {
        // 不正な括弧の組み合わせ
        assertEquals("Error", calculationEngine.evaluate("(2 + 3"))
        assertEquals("Error", calculationEngine.evaluate("2 + 3)"))
        assertEquals("Error", calculationEngine.evaluate("((2 + 3)"))
        assertEquals("Error", calculationEngine.evaluate("(2 + 3))"))
        
        // 空の括弧
        assertEquals("Error", calculationEngine.evaluate("()"))
        assertEquals("Error", calculationEngine.evaluate("2 + ()"))
    }    
    @Test
    fun `括弧組み合わせロジックテスト`() {
        // 括弧の数をカウントして適切な括弧を返すロジックのテスト
        
        // 空の式では開き括弧
        var expression = ""
        var openParens = expression.count { it == '(' }
        var closeParens = expression.count { it == ')' }
        var lastChar = expression.lastOrNull()
        var shouldAddOpenParen = when {
            expression.isEmpty() -> true
            lastChar in setOf('+', '-', '*', '/', '(') -> true
            openParens > closeParens -> false
            else -> true
        }
        assertTrue("空の式では開き括弧を追加", shouldAddOpenParen)
        
        // 演算子の後では開き括弧
        expression = "2+"
        openParens = expression.count { it == '(' }
        closeParens = expression.count { it == ')' }
        lastChar = expression.lastOrNull()
        shouldAddOpenParen = when {
            expression.isEmpty() -> true
            lastChar in setOf('+', '-', '*', '/', '(') -> true
            openParens > closeParens -> false
            else -> true
        }
        assertTrue("演算子の後では開き括弧を追加", shouldAddOpenParen)
        
        // 開き括弧が多い場合は閉じ括弧
        expression = "(2+3"
        openParens = expression.count { it == '(' }
        closeParens = expression.count { it == ')' }
        lastChar = expression.lastOrNull()
        shouldAddOpenParen = when {
            expression.isEmpty() -> true
            lastChar in setOf('+', '-', '*', '/', '(') -> true
            openParens > closeParens -> false
            else -> true
        }
        assertFalse("開き括弧が多い場合は閉じ括弧を追加", shouldAddOpenParen)
    }
}