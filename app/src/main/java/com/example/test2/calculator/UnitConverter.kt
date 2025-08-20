package com.example.test2.calculator

import com.example.test2.data.model.UnitCategory
import com.example.test2.data.model.UnitType
import com.example.test2.data.model.UnitDefinitions
import kotlin.math.pow

class UnitConverter {
    
    fun convert(value: Double, fromUnit: UnitType, toUnit: UnitType): Double {
        if (fromUnit.category != toUnit.category) {
            throw IllegalArgumentException("Cannot convert between different unit categories")
        }
        
        return when (fromUnit.category) {
            UnitCategory.TEMPERATURE -> convertTemperature(value, fromUnit, toUnit)
            else -> convertLinear(value, fromUnit, toUnit)
        }
    }
    
    private fun convertLinear(value: Double, fromUnit: UnitType, toUnit: UnitType): Double {
        val baseValue = value * fromUnit.baseMultiplier
        return baseValue / toUnit.baseMultiplier
    }
    
    private fun convertTemperature(value: Double, fromUnit: UnitType, toUnit: UnitType): Double {
        val celsius = when (fromUnit.id) {
            "celsius" -> value
            "fahrenheit" -> (value - 32) * 5.0 / 9.0
            "kelvin" -> value - 273.15
            else -> throw IllegalArgumentException("Unknown temperature unit: ${fromUnit.id}")
        }
        
        return when (toUnit.id) {
            "celsius" -> celsius
            "fahrenheit" -> celsius * 9.0 / 5.0 + 32
            "kelvin" -> celsius + 273.15
            else -> throw IllegalArgumentException("Unknown temperature unit: ${toUnit.id}")
        }
    }
    
    fun formatResult(value: Double): String {
        return when {
            value == 0.0 -> "0"
            value.isInfinite() -> if (value > 0) "∞" else "-∞"
            value.isNaN() -> "エラー"
            kotlin.math.abs(value) >= 1e15 -> String.format("%.3e", value)
            kotlin.math.abs(value) < 1e-10 && value != 0.0 -> String.format("%.3e", value)
            value == kotlin.math.floor(value) && kotlin.math.abs(value) < 1e10 -> value.toLong().toString()
            else -> {
                val formatted = String.format("%.10f", value).trimEnd('0').trimEnd('.')
                if (formatted.isEmpty()) "0" else formatted
            }
        }
    }
    
    fun getAllCategories(): List<UnitCategory> {
        return UnitCategory.values().toList()
    }
    
    fun getCategoryDisplayName(category: UnitCategory): String {
        return when (category) {
            UnitCategory.LENGTH -> "長さ"
            UnitCategory.WEIGHT -> "重量"
            UnitCategory.TEMPERATURE -> "温度"
            UnitCategory.AREA -> "面積"
            UnitCategory.VOLUME -> "体積"
            UnitCategory.TIME -> "時間"
        }
    }
    
    fun getUnitsForCategory(category: UnitCategory): List<UnitType> {
        return UnitDefinitions.getAllUnitsForCategory(category)
    }
    
    fun findUnitById(id: String): UnitType? {
        return UnitDefinitions.findUnit(id)
    }
}