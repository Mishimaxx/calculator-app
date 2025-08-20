package com.example.test2.data.model

enum class UnitCategory {
    LENGTH,     // 長さ
    WEIGHT,     // 重量
    TEMPERATURE, // 温度
    AREA,       // 面積
    VOLUME,     // 体積
    TIME        // 時間
}

data class UnitType(
    val id: String,
    val displayName: String,
    val symbol: String,
    val category: UnitCategory,
    val baseMultiplier: Double = 1.0 // ベース単位への変換率
)

object UnitDefinitions {
    val LENGTH_UNITS = listOf(
    // 指定順: mm, cm, dm, m, km, in, ft, yd, mi, 尺, 間, 町, 里, 海里
    UnitType("millimeter", "ミリメートル", "mm", UnitCategory.LENGTH, 0.001),
    UnitType("centimeter", "センチメートル", "cm", UnitCategory.LENGTH, 0.01),
    UnitType("decimeter", "デシメートル", "dm", UnitCategory.LENGTH, 0.1),
    UnitType("meter", "メートル", "m", UnitCategory.LENGTH, 1.0),
    UnitType("kilometer", "キロメートル", "km", UnitCategory.LENGTH, 1000.0),
    UnitType("inch", "インチ", "in", UnitCategory.LENGTH, 0.0254),
    UnitType("foot", "フィート", "ft", UnitCategory.LENGTH, 0.3048),
    UnitType("yard", "ヤード", "yd", UnitCategory.LENGTH, 0.9144),
    UnitType("mile", "マイル", "mi", UnitCategory.LENGTH, 1609.344),
    UnitType("shaku", "尺", "尺", UnitCategory.LENGTH, 10.0/33.0), // 約0.30303m
    UnitType("ken", "間", "間", UnitCategory.LENGTH, 20.0/11.0), // 6尺 ≈1.81818m
    UnitType("cho", "町", "町", UnitCategory.LENGTH, 1200.0/11.0), // 60間 ≈109.0909m
    UnitType("ri", "里", "里", UnitCategory.LENGTH, 43200.0/11.0), // 36町 ≈3927.2727m
    UnitType("nautical_mile", "海里", "海里", UnitCategory.LENGTH, 1852.0)
    )
    
    val WEIGHT_UNITS = listOf(
        UnitType("kilogram", "キログラム", "kg", UnitCategory.WEIGHT, 1.0),
        UnitType("gram", "グラム", "g", UnitCategory.WEIGHT, 0.001),
        UnitType("pound", "ポンド", "lb", UnitCategory.WEIGHT, 0.453592),
        UnitType("ounce", "オンス", "oz", UnitCategory.WEIGHT, 0.0283495),
        UnitType("ton", "トン", "t", UnitCategory.WEIGHT, 1000.0)
    )
    
    val TEMPERATURE_UNITS = listOf(
        UnitType("celsius", "摂氏", "°C", UnitCategory.TEMPERATURE),
        UnitType("fahrenheit", "華氏", "°F", UnitCategory.TEMPERATURE),
        UnitType("kelvin", "ケルビン", "K", UnitCategory.TEMPERATURE)
    )
    
    val AREA_UNITS = listOf(
        UnitType("square_meter", "平方メートル", "m²", UnitCategory.AREA, 1.0),
        UnitType("square_kilometer", "平方キロメートル", "km²", UnitCategory.AREA, 1000000.0),
        UnitType("square_centimeter", "平方センチメートル", "cm²", UnitCategory.AREA, 0.0001),
        UnitType("hectare", "ヘクタール", "ha", UnitCategory.AREA, 10000.0),
        UnitType("acre", "エーカー", "ac", UnitCategory.AREA, 4046.86)
    )
    
    val VOLUME_UNITS = listOf(
        UnitType("liter", "リットル", "L", UnitCategory.VOLUME, 1.0),
        UnitType("milliliter", "ミリリットル", "mL", UnitCategory.VOLUME, 0.001),
        UnitType("cubic_meter", "立方メートル", "m³", UnitCategory.VOLUME, 1000.0),
        UnitType("gallon", "ガロン", "gal", UnitCategory.VOLUME, 3.78541),
        UnitType("fluid_ounce", "液量オンス", "fl oz", UnitCategory.VOLUME, 0.0295735)
    )
    
    val TIME_UNITS = listOf(
        UnitType("second", "秒", "s", UnitCategory.TIME, 1.0),
        UnitType("minute", "分", "min", UnitCategory.TIME, 60.0),
        UnitType("hour", "時間", "h", UnitCategory.TIME, 3600.0),
        UnitType("day", "日", "d", UnitCategory.TIME, 86400.0),
        UnitType("week", "週", "w", UnitCategory.TIME, 604800.0),
        UnitType("month", "月", "mo", UnitCategory.TIME, 2629746.0),
        UnitType("year", "年", "y", UnitCategory.TIME, 31556952.0)
    )
    
    fun getAllUnitsForCategory(category: UnitCategory): List<UnitType> {
        return when (category) {
            UnitCategory.LENGTH -> LENGTH_UNITS
            UnitCategory.WEIGHT -> WEIGHT_UNITS
            UnitCategory.TEMPERATURE -> TEMPERATURE_UNITS
            UnitCategory.AREA -> AREA_UNITS
            UnitCategory.VOLUME -> VOLUME_UNITS
            UnitCategory.TIME -> TIME_UNITS
        }
    }
    
    fun findUnit(id: String): UnitType? {
        val allUnits = UnitCategory.values().flatMap { getAllUnitsForCategory(it) }
        return allUnits.find { it.id == id }
    }
}