package com.example.test2

import com.example.test2.calculator.CalculationEngine
import org.junit.Test
import org.junit.Assert.*

class DebugTest {
    private val calculationEngine = CalculationEngine()

    @Test
    fun testParenthesesCases() {
        println("Testing: (2 + 3")
        try {
            val result1 = calculationEngine.evaluate("(2 + 3")
            println("Result: $result1")
        } catch (e: Exception) {
            println("Exception: ${e.message}")
        }

        println("Testing: 2 + 3)")
        try {
            val result2 = calculationEngine.evaluate("2 + 3)")
            println("Result: $result2")
        } catch (e: Exception) {
            println("Exception: ${e.message}")
        }

        println("Testing: ()")
        try {
            val result3 = calculationEngine.evaluate("()")
            println("Result: $result3")
        } catch (e: Exception) {
            println("Exception: ${e.message}")
        }

        println("Testing: 2 + ()")
        try {
            val result4 = calculationEngine.evaluate("2 + ()")
            println("Result: $result4")
        } catch (e: Exception) {
            println("Exception: ${e.message}")
        }
    }
}
