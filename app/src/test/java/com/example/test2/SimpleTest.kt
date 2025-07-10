package com.example.test2

import org.junit.Test
import org.junit.Assert.*
import com.example.test2.calculator.CalculationEngine

class SimpleTest {
    
    @Test
    fun `空文字テスト`() {
        val calculationEngine = CalculationEngine()
        val result = calculationEngine.evaluate("")
        println("Result for empty string: '$result'")
        assertEquals("Error", result)
    }
}
