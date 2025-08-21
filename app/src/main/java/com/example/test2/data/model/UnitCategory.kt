package com.example.test2.data.model

enum class UnitCategory {
    LENGTH,     // 長さ
    WEIGHT,     // 質量（内部IDはWEIGHTのまま）
    TEMPERATURE, // 温度
    AREA,       // 面積
    VOLUME,     // 体積
    TIME,       // 時間
    FORCE,      // 力
    ENERGY,     // エネルギー
    POWER,      // 仕事量（出力）
    PRESSURE,   // 圧力
    SPEED,      // 速度
    FUEL_EFFICIENCY, // 燃料効率
    DATA_SIZE   // データ量
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
    
    // 質量（基準は kg） 指定順: ミリグラム, グラム, キログラム, トン, グレーン, オンス, ポンド, ストーン, ショートトン, ロングトン, トロイオンス, カラット, 匁, 両, 斤, 貫
    val WEIGHT_UNITS = listOf(
        UnitType("milligram", "ミリグラム", "mg", UnitCategory.WEIGHT, 0.000001),            // 1 mg = 1e-6 kg
        UnitType("gram", "グラム", "g", UnitCategory.WEIGHT, 0.001),                         // 1 g = 1e-3 kg
        UnitType("kilogram", "キログラム", "kg", UnitCategory.WEIGHT, 1.0),                   // 1 kg
        UnitType("ton", "トン", "t", UnitCategory.WEIGHT, 1000.0),                           // 1 t = 1000 kg（メートルトン）
        UnitType("grain", "グレーン", "gr", UnitCategory.WEIGHT, 0.00006479891),              // 1 gr = 64.79891 mg
        UnitType("ounce", "オンス", "oz", UnitCategory.WEIGHT, 0.028349523125),                // 1 oz (avoirdupois)
        UnitType("pound", "ポンド", "lb", UnitCategory.WEIGHT, 0.45359237),                   // 1 lb (avoirdupois)
        UnitType("stone", "ストーン", "st", UnitCategory.WEIGHT, 6.35029318),                 // 14 lb
        UnitType("short_ton", "ショートトン", "sh tn", UnitCategory.WEIGHT, 907.18474),         // 2000 lb (US)
        UnitType("long_ton", "ロングトン", "long t", UnitCategory.WEIGHT, 1016.0469088),       // 2240 lb (Imperial)
        UnitType("troy_ounce", "トロイオンス", "ozt", UnitCategory.WEIGHT, 0.0311034768),      // 31.1034768 g
        UnitType("carat", "カラット", "ct", UnitCategory.WEIGHT, 0.0002),                     // 0.2 g
        UnitType("monme", "匁", "匁", UnitCategory.WEIGHT, 0.00375),                          // 1 匁 = 3.75 g
        UnitType("ryo", "両", "両", UnitCategory.WEIGHT, 0.0375),                             // 1 両 = 10 匁 = 37.5 g
        UnitType("kin", "斤", "斤", UnitCategory.WEIGHT, 0.6),                               // 1 斤 = 160 匁 = 600 g
        UnitType("kan", "貫", "貫", UnitCategory.WEIGHT, 3.75)                                // 1 貫 = 1000 匁 = 3.75 kg
    )
    
    val TEMPERATURE_UNITS = listOf(
        UnitType("celsius", "摂氏温度", "°C", UnitCategory.TEMPERATURE),
        UnitType("fahrenheit", "華氏温度", "°F", UnitCategory.TEMPERATURE),
        UnitType("kelvin", "絶対温度", "K", UnitCategory.TEMPERATURE),
        UnitType("rankine", "ランキン温度", "°R", UnitCategory.TEMPERATURE)
    )
    
    val AREA_UNITS = listOf(
        // 指定順: 平方センチメートル, 平方デシメートル, 平方メートル, アール, ヘクタール, 平方キロメートル,
        // 平方インチ, 平方フィート, 平方ヤード, 平方マイル, エーカー, 平方尺, 畳(京間), 畳(中京間), 畳(江戸間), 畳(団地間), 坪, 反, 町歩
        UnitType("square_centimeter", "平方センチメートル", "cm²", UnitCategory.AREA, 0.0001),                 // 1 cm² = 1e-4 m²
        UnitType("square_decimeter", "平方デシメートル", "dm²", UnitCategory.AREA, 0.01),                    // 1 dm² = 1e-2 m²
        UnitType("square_meter", "平方メートル", "m²", UnitCategory.AREA, 1.0),                           // 1 m²
        UnitType("are", "アール", "a", UnitCategory.AREA, 100.0),                                         // 1 a = 100 m²
        UnitType("hectare", "ヘクタール", "ha", UnitCategory.AREA, 10000.0),                               // 1 ha = 10000 m²
        UnitType("square_kilometer", "平方キロメートル", "km²", UnitCategory.AREA, 1_000_000.0),             // 1 km² = 1e6 m²
        UnitType("square_inch", "平方インチ", "in²", UnitCategory.AREA, 0.00064516),                        // (0.0254 m)²
        UnitType("square_foot", "平方フィート", "ft²", UnitCategory.AREA, 0.09290304),                      // (0.3048 m)²
        UnitType("square_yard", "平方ヤード", "yd²", UnitCategory.AREA, 0.83612736),                        // (0.9144 m)²
        UnitType("square_mile", "平方マイル", "mi²", UnitCategory.AREA, 2_589_988.110336),                 // (1609.344 m)²
        UnitType("acre", "エーカー", "ac", UnitCategory.AREA, 4046.8564224),                               // exact
        UnitType("square_shaku", "平方尺", "尺²", UnitCategory.AREA, 100.0/1089.0),                         // (10/33 m)² = 100/1089 m²
        UnitType("tatami_kyo", "畳(京間)", "畳(京間)", UnitCategory.AREA, 1.824),                           // 約 0.955 m × 1.91 m
        UnitType("tatami_chukyo", "畳(中京間)", "畳(中京間)", UnitCategory.AREA, 1.6562),                    // 約 0.91 m × 1.82 m
        UnitType("tatami_edo", "畳(江戸間)", "畳(江戸間)", UnitCategory.AREA, 1.5488),                        // 約 0.88 m × 1.76 m
        UnitType("tatami_danchi", "畳(団地間)", "畳(団地間)", UnitCategory.AREA, 1.445),                    // 約 0.85 m × 1.70 m
        UnitType("tsubo", "坪", "坪", UnitCategory.AREA, 400.0/121.0),                                     // 1 坪 = 400/121 m² ≈ 3.305785
        UnitType("tan", "反", "反", UnitCategory.AREA, 120000.0/121.0),                                    // 1 反 = 300 坪 = 120000/121 m² ≈ 991.735537
        UnitType("cho_bu", "町歩", "町歩", UnitCategory.AREA, 1_200_000.0/121.0)                            // 1 町歩 = 3000 坪 = 1200000/121 m² ≈ 9917.355372
    )
    
    val VOLUME_UNITS = listOf(
        // 指定順: ミリリットル, センチリットル, デシリットル, リットル, ヘクトリットル, キロリットル,
        // 立方センチメートル, 立方デシメートル, 立方メートル, 立方インチ, 立方フィート, 立方ヤード,
        // ガロン(英), クォート(英), パイント(英), 液量オンス(英),
        // ガロン(米), クォート(米), パイント(米), カップ(米), 液量オンス(米), テーブルスプーン(米), ティースプーン(米),
        // カップ(200mL), カップ(240mL), カップ(250mL), テーブルスプーン(15mL), ティースプーン(5mL),
        // バレル, 合, 升, 斗, 石
        UnitType("milliliter", "ミリリットル", "mL", UnitCategory.VOLUME, 0.001),
        UnitType("centiliter", "センチリットル", "cL", UnitCategory.VOLUME, 0.01),
        UnitType("deciliter", "デシリットル", "dL", UnitCategory.VOLUME, 0.1),
        UnitType("liter", "リットル", "L", UnitCategory.VOLUME, 1.0),
        UnitType("hectoliter", "ヘクトリットル", "hL", UnitCategory.VOLUME, 100.0),
        UnitType("kiloliter", "キロリットル", "kL", UnitCategory.VOLUME, 1000.0),

        UnitType("cubic_centimeter", "立方センチメートル", "cm³", UnitCategory.VOLUME, 0.001), // 1 cm³ = 1 mL
        UnitType("cubic_decimeter", "立方デシメートル", "dm³", UnitCategory.VOLUME, 1.0), // 1 dm³ = 1 L
        UnitType("cubic_meter", "立方メートル", "m³", UnitCategory.VOLUME, 1000.0),

        UnitType("cubic_inch", "立方インチ", "in³", UnitCategory.VOLUME, 0.016387064), // exact
        UnitType("cubic_foot", "立方フィート", "ft³", UnitCategory.VOLUME, 28.316846592), // exact
        UnitType("cubic_yard", "立方ヤード", "yd³", UnitCategory.VOLUME, 764.554857984), // exact

        UnitType("imperial_gallon", "ガロン(英)", "gal (Imp)", UnitCategory.VOLUME, 4.54609), // exact
        UnitType("imperial_quart", "クォート(英)", "qt (Imp)", UnitCategory.VOLUME, 1.1365225),
        UnitType("imperial_pint", "パイント(英)", "pt (Imp)", UnitCategory.VOLUME, 0.56826125),
        UnitType("imperial_fl_ounce", "液量オンス(英)", "fl oz (Imp)", UnitCategory.VOLUME, 0.0284130625),

        UnitType("us_gallon", "ガロン(米)", "gal (US)", UnitCategory.VOLUME, 3.785411784), // exact
        UnitType("us_quart", "クォート(米)", "qt (US)", UnitCategory.VOLUME, 0.946352946), // exact
        UnitType("us_pint", "パイント(米)", "pt (US)", UnitCategory.VOLUME, 0.473176473), // exact
        UnitType("us_cup", "カップ(米)", "cup (US)", UnitCategory.VOLUME, 0.2365882365),
        UnitType("us_fl_ounce", "液量オンス(米)", "fl oz (US)", UnitCategory.VOLUME, 0.0295735295625), // exact
        UnitType("us_tablespoon", "テーブルスプーン(米)", "tbsp (US)", UnitCategory.VOLUME, 0.01478676478125),
        UnitType("us_teaspoon", "ティースプーン(米)", "tsp (US)", UnitCategory.VOLUME, 0.00492892159375),

        UnitType("cup_200ml", "カップ(200mL)", "cup (200mL)", UnitCategory.VOLUME, 0.2),
        UnitType("cup_240ml", "カップ(240mL)", "cup (240mL)", UnitCategory.VOLUME, 0.24),
        UnitType("cup_250ml", "カップ(250mL)", "cup (250mL)", UnitCategory.VOLUME, 0.25),
        UnitType("tablespoon_15ml", "テーブルスプーン(15mL)", "tbsp (15mL)", UnitCategory.VOLUME, 0.015),
        UnitType("teaspoon_5ml", "ティースプーン(5mL)", "tsp (5mL)", UnitCategory.VOLUME, 0.005),

        UnitType("barrel", "バレル", "bbl", UnitCategory.VOLUME, 158.987294928), // 42 US gal

        UnitType("go", "合", "合", UnitCategory.VOLUME, 0.18039),
        UnitType("sho", "升", "升", UnitCategory.VOLUME, 1.8039), // 10 合
        UnitType("to", "斗", "斗", UnitCategory.VOLUME, 18.039), // 10 升
        UnitType("koku", "石", "石", UnitCategory.VOLUME, 180.39) // 10 斗
    )
    
    val TIME_UNITS = listOf(
        UnitType("millisecond", "ミリ秒", "ms", UnitCategory.TIME, 0.001),
        UnitType("second", "秒", "s", UnitCategory.TIME, 1.0),
        UnitType("minute", "分", "min", UnitCategory.TIME, 60.0),
        UnitType("hour", "時間", "h", UnitCategory.TIME, 3600.0),
        UnitType("day", "日", "d", UnitCategory.TIME, 86400.0),
        UnitType("week", "週", "w", UnitCategory.TIME, 604800.0),
        UnitType("month", "月", "mo", UnitCategory.TIME, 2629746.0),
        UnitType("year", "年", "y", UnitCategory.TIME, 31556952.0)
    )

    // 力（基準は N）
    val FORCE_UNITS = listOf(
        UnitType("newton", "ニュートン", "N", UnitCategory.FORCE, 1.0),
        UnitType("kilonewton", "キロニュートン", "kN", UnitCategory.FORCE, 1000.0),
        UnitType("dyne", "ダイン", "dyn", UnitCategory.FORCE, 1e-5),
        UnitType("gram_force", "重量グラム", "gf", UnitCategory.FORCE, 0.00980665),
        UnitType("kilogram_force", "重量キログラム", "kgf", UnitCategory.FORCE, 9.80665),
        UnitType("pound_force", "重量ポンド", "lbf", UnitCategory.FORCE, 4.4482216152605),
        UnitType("poundal", "パウンダル", "pdl", UnitCategory.FORCE, 0.138254954376)
    )

    // エネルギー（基準は J）
    val ENERGY_UNITS = listOf(
        // 指定順: ジュール, キロジュール, メガジュール, ギガジュール, カロリー, キロカロリー,
        // ワット秒, ワット時, キロワット時, メガワット時, ギガワット時, 英熱量, ニュートンメートル,
        // 重量キログラムメートル, 重量ポンドインチ, 重量ポンドフィート
        UnitType("joule", "ジュール", "J", UnitCategory.ENERGY, 1.0),
        UnitType("kilojoule", "キロジュール", "kJ", UnitCategory.ENERGY, 1_000.0),
        UnitType("megajoule", "メガジュール", "MJ", UnitCategory.ENERGY, 1_000_000.0),
        UnitType("gigajoule", "ギガジュール", "GJ", UnitCategory.ENERGY, 1_000_000_000.0),
        UnitType("calorie", "カロリー", "cal", UnitCategory.ENERGY, 4.184),
        UnitType("kilocalorie", "キロカロリー", "kcal", UnitCategory.ENERGY, 4_184.0),
        UnitType("watt_second", "ワット秒", "Ws", UnitCategory.ENERGY, 1.0),
        UnitType("watt_hour", "ワット時", "Wh", UnitCategory.ENERGY, 3_600.0),
        UnitType("kilowatt_hour", "キロワット時", "kWh", UnitCategory.ENERGY, 3_600_000.0),
        UnitType("megawatt_hour", "メガワット時", "MWh", UnitCategory.ENERGY, 3_600_000_000.0),
        UnitType("gigawatt_hour", "ギガワット時", "GWh", UnitCategory.ENERGY, 3_600_000_000_000.0),
        UnitType("btu", "英熱量", "Btu", UnitCategory.ENERGY, 1_055.05585262),
        UnitType("newton_meter", "ニュートンメートル", "N·m", UnitCategory.ENERGY, 1.0),
        UnitType("kilogram_force_meter", "重量キログラムメートル", "kgf·m", UnitCategory.ENERGY, 9.80665),
        UnitType("pound_force_inch", "重量ポンドインチ", "lbf·in", UnitCategory.ENERGY, 0.1129848290276167),
        UnitType("pound_force_foot", "重量ポンドフィート", "lbf·ft", UnitCategory.ENERGY, 1.3558179483314004)
    )

    // 仕事量（出力, 基準は W）
    val POWER_UNITS = listOf(
        // 指定順: ミリワット, ワット, キロワット, メガワット, 馬力(HP), 馬力(PS), デシベルミリワット, デシベルワット,
        // 英熱量毎時, kcal/h, Mcal/h, Gcal/h
        UnitType("milliwatt", "ミリワット", "mW", UnitCategory.POWER, 0.001),
        UnitType("watt", "ワット", "W", UnitCategory.POWER, 1.0),
        UnitType("kilowatt", "キロワット", "kW", UnitCategory.POWER, 1000.0),
        UnitType("megawatt", "メガワット", "MW", UnitCategory.POWER, 1_000_000.0),
        UnitType("horsepower_hp", "馬力(HP)", "HP", UnitCategory.POWER, 745.699871582),
        UnitType("horsepower_ps", "馬力(PS)", "PS", UnitCategory.POWER, 735.49875),
        UnitType("decibel_milliwatt", "デシベルミリワット", "dBm", UnitCategory.POWER, 1.0), // 対数換算（特別処理）
        UnitType("decibel_watt", "デシベルワット", "dBW", UnitCategory.POWER, 1.0),         // 対数換算（特別処理）
        UnitType("btu_per_hour", "英熱量毎時", "Btu/h", UnitCategory.POWER, 0.29307107),
        UnitType("kcal_per_hour", "kcal/h", "kcal/h", UnitCategory.POWER, 1.1622222222222223),
        UnitType("Mcal_per_hour", "Mcal/h", "Mcal/h", UnitCategory.POWER, 1162.2222222222222),
        UnitType("Gcal_per_hour", "Gcal/h", "Gcal/h", UnitCategory.POWER, 1_162_222.2222222222)
    )

    // 圧力（基準は Pa） 指定順: 気圧, トル, パスカル, ヘクトパスカル, キロパスカル, メガパスカル, ダイン毎平方センチメートル,
    // ミリバール, バール, kN/m², kgf/cm², プサイ, 水銀柱ミリメートル, 水銀柱インチ, 水柱ミリメートル, 水柱インチ
    val PRESSURE_UNITS = listOf(
        UnitType("atmosphere", "気圧", "atm", UnitCategory.PRESSURE, 101325.0),
        UnitType("torr", "トル", "Torr", UnitCategory.PRESSURE, 101325.0 / 760.0),
        UnitType("pascal", "パスカル", "Pa", UnitCategory.PRESSURE, 1.0),
        UnitType("hectopascal", "ヘクトパスカル", "hPa", UnitCategory.PRESSURE, 100.0),
        UnitType("kilopascal", "キロパスカル", "kPa", UnitCategory.PRESSURE, 1000.0),
        UnitType("megapascal", "メガパスカル", "MPa", UnitCategory.PRESSURE, 1_000_000.0),
        UnitType("dyne_per_cm2", "ダイン毎平方センチメートル", "dyn/cm²", UnitCategory.PRESSURE, 0.1),
        UnitType("millibar", "ミリバール", "mbar", UnitCategory.PRESSURE, 100.0),
        UnitType("bar", "バール", "bar", UnitCategory.PRESSURE, 100_000.0),
        UnitType("kilo_newton_per_m2", "kN/m²", "kN/m²", UnitCategory.PRESSURE, 1_000.0),
        UnitType("kgf_per_cm2", "kgf/cm²", "kgf/cm²", UnitCategory.PRESSURE, 98_066.5),
        UnitType("psi", "プサイ", "psi", UnitCategory.PRESSURE, 6_894.757293168),
        UnitType("mmHg", "水銀柱ミリメートル", "mmHg", UnitCategory.PRESSURE, 133.322387415),
        UnitType("inHg", "水銀柱インチ", "inHg", UnitCategory.PRESSURE, 3_386.38815789),
        UnitType("mmH2O", "水柱ミリメートル", "mmH2O", UnitCategory.PRESSURE, 9.80665),
        UnitType("inH2O", "水柱インチ", "inH2O", UnitCategory.PRESSURE, 249.08891)
    )

    // 速度（基準は m/s）
    val SPEED_UNITS = listOf(
        UnitType("meter_per_second", "m/s", "m/s", UnitCategory.SPEED, 1.0),
        UnitType("meter_per_hour", "m/h", "m/h", UnitCategory.SPEED, 1.0 / 3600.0),
        UnitType("kilometer_per_second", "km/s", "km/s", UnitCategory.SPEED, 1000.0),
        UnitType("kilometer_per_hour", "km/h", "km/h", UnitCategory.SPEED, 1000.0 / 3600.0),
        UnitType("inch_per_second", "in/s", "in/s", UnitCategory.SPEED, 0.0254),
        UnitType("inch_per_hour", "in/h", "in/h", UnitCategory.SPEED, 0.0254 / 3600.0),
        UnitType("foot_per_second", "ft/s", "ft/s", UnitCategory.SPEED, 0.3048),
        UnitType("foot_per_hour", "ft/h", "ft/h", UnitCategory.SPEED, 0.3048 / 3600.0),
        UnitType("mile_per_second", "mi/s", "mi/s", UnitCategory.SPEED, 1609.344),
        UnitType("mile_per_hour", "mi/h", "mi/h", UnitCategory.SPEED, 1609.344 / 3600.0),
        UnitType("knot", "ノット", "kn", UnitCategory.SPEED, 1852.0 / 3600.0),
        UnitType("mach", "マッハ", "Mach", UnitCategory.SPEED, 340.29)
    )

    // 燃料効率（特殊換算）
    val FUEL_EFFICIENCY_UNITS = listOf(
        UnitType("km_per_liter", "km/ℓ", "km/L", UnitCategory.FUEL_EFFICIENCY, 1.0),
        UnitType("mile_per_liter", "mi/ℓ", "mi/L", UnitCategory.FUEL_EFFICIENCY, 1.0),
        UnitType("liter_per_100km", "ℓ/100km", "L/100km", UnitCategory.FUEL_EFFICIENCY, 1.0),
        UnitType("km_per_gallon_imp", "km/gal(英)", "km/gal (Imp)", UnitCategory.FUEL_EFFICIENCY, 1.0),
        UnitType("mile_per_gallon_imp", "mi/gal(英)", "mpg (Imp)", UnitCategory.FUEL_EFFICIENCY, 1.0),
        UnitType("gallon_per_100mile_imp", "gal/100mi(英)", "gal/100mi (Imp)", UnitCategory.FUEL_EFFICIENCY, 1.0),
        UnitType("km_per_gallon_us", "km/gal(米)", "km/gal (US)", UnitCategory.FUEL_EFFICIENCY, 1.0),
        UnitType("mile_per_gallon_us", "mi/gal(米)", "mpg (US)", UnitCategory.FUEL_EFFICIENCY, 1.0),
        UnitType("gallon_per_100mile_us", "gal/100mi(米)", "gal/100mi (US)", UnitCategory.FUEL_EFFICIENCY, 1.0)
    )

    // データ量（基準は バイト）
    val DATA_SIZE_UNITS = listOf(
        UnitType("bit", "ビット", "b", UnitCategory.DATA_SIZE, 0.125),
        UnitType("byte", "バイト", "B", UnitCategory.DATA_SIZE, 1.0),
        UnitType("kilobyte", "キロバイト", "KB", UnitCategory.DATA_SIZE, 1_000.0),
        UnitType("megabyte", "メガバイト", "MB", UnitCategory.DATA_SIZE, 1_000_000.0),
        UnitType("gigabyte", "ギガバイト", "GB", UnitCategory.DATA_SIZE, 1_000_000_000.0),
        UnitType("terabyte", "テラバイト", "TB", UnitCategory.DATA_SIZE, 1_000_000_000_000.0),
        UnitType("petabyte", "ペタバイト", "PB", UnitCategory.DATA_SIZE, 1_000_000_000_000_000.0)
    )
    
    fun getAllUnitsForCategory(category: UnitCategory): List<UnitType> {
        return when (category) {
            UnitCategory.LENGTH -> LENGTH_UNITS
            UnitCategory.WEIGHT -> WEIGHT_UNITS
            UnitCategory.TEMPERATURE -> TEMPERATURE_UNITS
            UnitCategory.AREA -> AREA_UNITS
            UnitCategory.VOLUME -> VOLUME_UNITS
            UnitCategory.TIME -> TIME_UNITS
            UnitCategory.FORCE -> FORCE_UNITS
            UnitCategory.ENERGY -> ENERGY_UNITS
            UnitCategory.POWER -> POWER_UNITS
            UnitCategory.PRESSURE -> PRESSURE_UNITS
            UnitCategory.SPEED -> SPEED_UNITS
            UnitCategory.FUEL_EFFICIENCY -> FUEL_EFFICIENCY_UNITS
            UnitCategory.DATA_SIZE -> DATA_SIZE_UNITS
        }
    }
    
    fun findUnit(id: String): UnitType? {
        val allUnits = UnitCategory.values().flatMap { getAllUnitsForCategory(it) }
        return allUnits.find { it.id == id }
    }
}