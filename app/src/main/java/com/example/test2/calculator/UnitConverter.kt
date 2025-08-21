package com.example.test2.calculator

import com.example.test2.data.model.UnitCategory
import com.example.test2.data.model.UnitType
import com.example.test2.data.model.UnitDefinitions
import kotlin.math.pow
import kotlin.math.log10
import java.math.BigDecimal
import java.math.RoundingMode

class UnitConverter {
    
    fun convert(value: Double, fromUnit: UnitType, toUnit: UnitType): Double {
        if (fromUnit.category != toUnit.category) {
            throw IllegalArgumentException("Cannot convert between different unit categories")
        }
        
        return when (fromUnit.category) {
            UnitCategory.TEMPERATURE -> convertTemperature(value, fromUnit, toUnit)
            UnitCategory.POWER -> convertPower(value, fromUnit, toUnit)
            UnitCategory.FUEL_EFFICIENCY -> convertFuelEfficiency(value, fromUnit, toUnit)
            else -> convertLinear(value, fromUnit, toUnit)
        }
    }
    
    private fun convertLinear(value: Double, fromUnit: UnitType, toUnit: UnitType): Double {
        // BigDecimalを使用して精密な計算を行う
        val valueBD = BigDecimal(value.toString())
        val fromMultiplierBD = BigDecimal(fromUnit.baseMultiplier.toString())
        val toMultiplierBD = BigDecimal(toUnit.baseMultiplier.toString())
        
        val baseValue = valueBD.multiply(fromMultiplierBD)
        val result = baseValue.divide(toMultiplierBD, 12, RoundingMode.HALF_UP)
        
        return result.toDouble()
    }
    
    private fun convertTemperature(value: Double, fromUnit: UnitType, toUnit: UnitType): Double {
        val celsius = when (fromUnit.id) {
            "celsius" -> value
            "fahrenheit" -> (value - 32) * 5.0 / 9.0
            "kelvin" -> value - 273.15
            "rankine" -> (value - 491.67) * 5.0 / 9.0
            else -> throw IllegalArgumentException("Unknown temperature unit: ${fromUnit.id}")
        }
        
        return when (toUnit.id) {
            "celsius" -> celsius
            "fahrenheit" -> celsius * 9.0 / 5.0 + 32
            "kelvin" -> celsius + 273.15
            "rankine" -> (celsius + 273.15) * 9.0 / 5.0
            else -> throw IllegalArgumentException("Unknown temperature unit: ${toUnit.id}")
        }
    }

    private fun convertPower(value: Double, fromUnit: UnitType, toUnit: UnitType): Double {
        fun isDb(unit: UnitType): Boolean = unit.id == "decibel_milliwatt" || unit.id == "decibel_watt"

        // 1) まずワット(W)に変換
        val watts: Double = if (isDb(fromUnit)) {
            when (fromUnit.id) {
                "decibel_milliwatt" -> 10.0.pow((value - 30.0) / 10.0) // dBm → W
                "decibel_watt" -> 10.0.pow(value / 10.0)                // dBW → W
                else -> Double.NaN
            }
        } else {
            // 線形単位は multiplier を用いて W へ
            BigDecimal(value.toString()).multiply(BigDecimal(fromUnit.baseMultiplier.toString())).toDouble()
        }

        // 2) 目的の単位へ
        return if (isDb(toUnit)) {
            when (toUnit.id) {
                "decibel_milliwatt" -> if (watts > 0.0) 10.0 * log10(watts) + 30.0 else Double.NEGATIVE_INFINITY
                "decibel_watt" -> if (watts > 0.0) 10.0 * log10(watts) else Double.NEGATIVE_INFINITY
                else -> Double.NaN
            }
        } else {
            // 線形単位へは W を割る
            val toMul = BigDecimal(toUnit.baseMultiplier.toString())
            BigDecimal(watts.toString()).divide(toMul, 12, RoundingMode.HALF_UP).toDouble()
        }
    }
    
    fun formatResult(value: Double): String {
        return when {
            value == 0.0 -> "0"
            value.isInfinite() -> if (value > 0) "∞" else "-∞"
            value.isNaN() -> "エラー"
            kotlin.math.abs(value) >= 1e15 -> String.format("%.3e", value)
            kotlin.math.abs(value) < 1e-10 && value != 0.0 -> String.format("%.3e", value)
            else -> {
                // BigDecimalを使用してより正確な表示を行う
                val bd = BigDecimal(value.toString()).stripTrailingZeros()
                val plainString = bd.toPlainString()
                addCommas(plainString)
            }
        }
    }
    
    private fun addCommas(numberString: String): String {
        val parts = numberString.split(".")
        val integerPart = parts[0]
        val decimalPart = if (parts.size > 1) parts[1] else ""
        
        // 整数部分に3桁区切りを追加
        val formattedInteger = StringBuilder()
        val reversed = integerPart.reversed()
        for (i in reversed.indices) {
            if (i > 0 && i % 3 == 0) {
                formattedInteger.append(",")
            }
            formattedInteger.append(reversed[i])
        }
        
        val result = formattedInteger.reverse().toString()
        return if (decimalPart.isNotEmpty()) "$result.$decimalPart" else result
    }

    private fun convertFuelEfficiency(value: Double, fromUnit: UnitType, toUnit: UnitType): Double {
        // 中間表現を km/L とする（高いほど効率が良い）
        fun toKmPerL(v: Double, u: UnitType): Double {
            return when (u.id) {
                "km_per_liter" -> v
                "mile_per_liter" -> v * 1.609344
                "liter_per_100km" -> if (v == 0.0) Double.POSITIVE_INFINITY else 100.0 / v
                "km_per_gallon_imp" -> v * 4.54609
                "mile_per_gallon_imp" -> v * 1.609344 * 4.54609
                "gallon_per_100mile_imp" -> if (v == 0.0) Double.POSITIVE_INFINITY else (100.0 * 1.609344) / (v * 4.54609)
                "km_per_gallon_us" -> v * 3.785411784
                "mile_per_gallon_us" -> v * 1.609344 * 3.785411784
                "gallon_per_100mile_us" -> if (v == 0.0) Double.POSITIVE_INFINITY else (100.0 * 1.609344) / (v * 3.785411784)
                else -> throw IllegalArgumentException("Unknown fuel efficiency unit: ${u.id}")
            }
        }
        fun fromKmPerL(kmpl: Double, u: UnitType): Double {
            return when (u.id) {
                "km_per_liter" -> kmpl
                "mile_per_liter" -> kmpl / 1.609344
                "liter_per_100km" -> if (kmpl == 0.0) Double.POSITIVE_INFINITY else 100.0 / kmpl
                "km_per_gallon_imp" -> kmpl / 4.54609
                "mile_per_gallon_imp" -> kmpl / (1.609344 * 4.54609)
                "gallon_per_100mile_imp" -> if (kmpl == 0.0) Double.POSITIVE_INFINITY else (100.0 * 1.609344) / (kmpl * 4.54609)
                "km_per_gallon_us" -> kmpl / 3.785411784
                "mile_per_gallon_us" -> kmpl / (1.609344 * 3.785411784)
                "gallon_per_100mile_us" -> if (kmpl == 0.0) Double.POSITIVE_INFINITY else (100.0 * 1.609344) / (kmpl * 3.785411784)
                else -> throw IllegalArgumentException("Unknown fuel efficiency unit: ${u.id}")
            }
        }
        val kmpl = toKmPerL(value, fromUnit)
        return fromKmPerL(kmpl, toUnit)
    }
    
    fun getAllCategories(): List<UnitCategory> {
        return UnitCategory.values().toList()
    }
    
    fun getCategoryDisplayName(category: UnitCategory): String {
        return when (category) {
            UnitCategory.LENGTH -> "長さ"
            UnitCategory.WEIGHT -> "質量"
            UnitCategory.TEMPERATURE -> "温度"
            UnitCategory.AREA -> "面積"
            UnitCategory.VOLUME -> "体積"
            UnitCategory.TIME -> "時間"
            UnitCategory.FORCE -> "力"
            UnitCategory.ENERGY -> "エネルギー"
            UnitCategory.POWER -> "仕事量"
            UnitCategory.PRESSURE -> "圧力"
            UnitCategory.SPEED -> "速度"
            UnitCategory.FUEL_EFFICIENCY -> "燃料効率"
            UnitCategory.DATA_SIZE -> "データ量"
        }
    }
    
    fun getUnitsForCategory(category: UnitCategory): List<UnitType> {
        return UnitDefinitions.getAllUnitsForCategory(category)
    }
    
    fun findUnitById(id: String): UnitType? {
        return UnitDefinitions.findUnit(id)
    }
}