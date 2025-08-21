package com.example.test2.calculator

import java.math.BigDecimal
import java.math.RoundingMode
import android.util.Log
import org.json.JSONObject

/**
 * 通貨換算（簡易・固定レート）
 * ratePerUSD: 1 USD = ratePerUSD [currency]
 */
data class CurrencyType(
    val code: String,
    val displayName: String,
    var ratePerUSD: Double
)

object CurrencyDefinitions {
    // Mutable so we can update rates
    val CURRENCIES: MutableList<CurrencyType> = mutableListOf(
        CurrencyType("USD", "米ドル", 1.0),
        CurrencyType("EUR", "ユーロ", 0.91),
        CurrencyType("GBP", "英ポンド", 0.76),
        CurrencyType("HKD", "香港ドル", 7.80),
        CurrencyType("CNY", "人民元", 7.25),
        CurrencyType("JPY", "日本円", 155.0),
        CurrencyType("AUD", "オーストラリアドル", 1.50),
        CurrencyType("CAD", "カナダドル", 1.37),
        CurrencyType("INR", "インドルピー", 83.0),
        CurrencyType("KRW", "韓国ウォン", 1350.0)
    )

    fun find(code: String): CurrencyType? = CURRENCIES.find { it.code == code }
    fun updateRates(newRates: Map<String, Double>) {
        CURRENCIES.forEach { cur ->
            newRates[cur.code]?.let { cur.ratePerUSD = it }
        }
    }
    
    // 追加通貨（初期レートは仮: 1.0＝更新前プレースホルダ。更新ボタン押下で正しい値に置換）
    private val ADDITIONAL: List<CurrencyType> = listOf(
        CurrencyType("AED", "アラブ首長国連邦ディルハム", 3.67),
        CurrencyType("AFN", "アフガニスタン・アフガニ", 1.0),
        CurrencyType("ALL", "アルバニア・レク", 1.0),
        CurrencyType("AMD", "アルメニア・ドラム", 1.0),
        CurrencyType("ANG", "オランダ領アンティル・ギルダー", 1.0),
        CurrencyType("AOA", "アンゴラ・クワンザ", 1.0),
        CurrencyType("ARS", "アルゼンチン・ペソ", 1.0),
        CurrencyType("AWG", "アルバ・フロリン", 1.0),
        CurrencyType("AZN", "アゼルバイジャン・マナト", 1.0),
        CurrencyType("BAM", "ボスニア・ヘルツェゴビナ・兌換マルク", 1.0),
        CurrencyType("BBD", "バルバドス・ドル", 1.0),
        CurrencyType("BDT", "バングラデシュ・タカ", 1.0),
        CurrencyType("BGN", "ブルガリア・レフ", 1.0),
        CurrencyType("BHD", "バーレーン・ディナール", 1.0),
        CurrencyType("BIF", "ブルンジ・フラン", 1.0),
        CurrencyType("BMD", "バミューダ・ドル", 1.0),
        CurrencyType("BND", "ブルネイ・ドル", 1.0),
        CurrencyType("BOB", "ボリビア・ボリビアーノ", 1.0),
        CurrencyType("BRL", "ブラジル・レアル", 1.0),
        CurrencyType("BSD", "バハマ・ドル", 1.0),
        CurrencyType("BTC", "ビットコイン", 0.00002), // 便宜上(約 1BTC=~50000USD想定) 1USD=0.00002BTC
        CurrencyType("BTN", "ブータン・ニュルタム", 1.0),
        CurrencyType("BWP", "ボツワナ・プラ", 1.0),
        CurrencyType("BYN", "ベラルーシ・ルーブル", 1.0),
        CurrencyType("BZD", "ベリーズ・ドル", 1.0),
        CurrencyType("CDF", "コンゴ・フラン", 1.0),
        CurrencyType("CHF", "スイス・フラン", 0.9),
        CurrencyType("CLF", "チリ・ウニダ・デ・フォメント", 0.0003),
        CurrencyType("CLP", "チリ・ペソ", 1.0),
        CurrencyType("CNH", "人民元(オフショア)", 7.25),
        CurrencyType("COP", "コロンビア・ペソ", 1.0),
        CurrencyType("CRC", "コスタリカ・コロン", 1.0),
        CurrencyType("CUP", "キューバ・ペソ", 1.0),
        CurrencyType("CVE", "カーボベルデ・エスクード", 1.0),
        CurrencyType("CZK", "チェコ・コルナ", 1.0),
        CurrencyType("DJF", "ジブチ・フラン", 1.0),
        CurrencyType("DKK", "デンマーク・クローネ", 1.0),
        CurrencyType("DOP", "ドミニカ・ペソ", 1.0),
        CurrencyType("DZD", "アルジェリア・ディナール", 1.0),
        CurrencyType("EGP", "エジプト・ポンド", 1.0),
        CurrencyType("ETB", "エチオピア・ブル", 1.0),
        CurrencyType("ETH", "エーテル", 0.0003), // 約 1ETH=~3000USD 想定
        CurrencyType("FJD", "フィジー・ドル", 1.0),
        CurrencyType("GEL", "ジョージア・ラリ", 1.0),
        CurrencyType("GHS", "ガーナ・セディ", 1.0),
        CurrencyType("GMD", "ガンビア・ダラシ", 1.0),
        CurrencyType("GNF", "ギニア・フラン", 1.0),
        CurrencyType("GTQ", "グアテマラ・ケツァル", 1.0),
        CurrencyType("GYD", "ガイアナ・ドル", 1.0),
        CurrencyType("HNL", "ホンジュラス・レンピラ", 1.0),
        CurrencyType("HTG", "ハイチ・グールド", 1.0),
        CurrencyType("HUF", "ハンガリー・フォリント", 1.0),
        CurrencyType("IDR", "インドネシア・ルピア", 1.0),
        CurrencyType("ILS", "イスラエル・新シェケル", 1.0),
        CurrencyType("IQD", "イラク・ディナール", 1.0),
        CurrencyType("IRR", "イラン・リヤル", 1.0),
        CurrencyType("ISK", "アイスランド・クローナ", 1.0),
        CurrencyType("JMD", "ジャマイカ・ドル", 1.0),
        CurrencyType("JOD", "ヨルダン・ディナール", 1.0),
        CurrencyType("KES", "ケニア・シリング", 1.0),
        CurrencyType("KHR", "カンボジア・リエル", 1.0),
        CurrencyType("KMF", "コモロ・フラン", 1.0),
        CurrencyType("KWD", "クウェート・ディナール", 0.31),
        CurrencyType("KYD", "ケイマン諸島・ドル", 1.0),
        CurrencyType("KZT", "カザフスタン・テンゲ", 1.0),
        CurrencyType("LAK", "ラオス・キープ", 1.0),
        CurrencyType("LBP", "レバノン・ポンド", 1.0),
        CurrencyType("LKR", "スリランカ・ルピー", 1.0),
        CurrencyType("LRD", "リベリア・ドル", 1.0),
        CurrencyType("LSL", "レソト・ロティ", 1.0),
        CurrencyType("LTC", "ライトコイン", 0.015),
        CurrencyType("LYD", "リビア・ディナール", 1.0),
        CurrencyType("MAD", "モロッコ・ディルハム", 1.0),
        CurrencyType("MDL", "モルドバ・レウ", 1.0),
        CurrencyType("MGA", "マダガスカル・アリアリ", 1.0),
        CurrencyType("MKD", "マケドニア・デナール", 1.0),
        CurrencyType("MMK", "ミャンマー・チャット", 1.0),
        CurrencyType("MOP", "マカオ・パタカ", 1.0),
        CurrencyType("MRU", "モーリタニア・ウギア", 1.0),
        CurrencyType("MUR", "モーリシャス・ルピー", 1.0),
        CurrencyType("MVR", "モルディブ・ルフィヤ", 1.0),
        CurrencyType("MWK", "マラウイ・クワチャ", 1.0),
        CurrencyType("MXN", "メキシコ・ペソ", 1.0),
        CurrencyType("MYR", "マレーシア・リンギット", 1.0),
        CurrencyType("MZN", "モザンビーク・メティカル", 1.0),
        CurrencyType("NAD", "ナミビア・ドル", 1.0),
        CurrencyType("NGN", "ナイジェリア・ナイラ", 1.0),
        CurrencyType("NIO", "ニカラグア・コルドバ", 1.0),
        CurrencyType("NOK", "ノルウェー・クローネ", 1.0),
        CurrencyType("NPR", "ネパール・ルピー", 1.0),
        CurrencyType("NZD", "ニュージーランド・ドル", 1.0),
        CurrencyType("OMR", "オマーン・リヤル", 0.39),
        CurrencyType("PAB", "パナマ・バルボア", 1.0),
        CurrencyType("PEN", "ペルー・ソル", 1.0),
        CurrencyType("PGK", "パプアニューギニア・キナ", 1.0),
        CurrencyType("PHP", "フィリピン・ペソ", 1.0),
        CurrencyType("PKR", "パキスタン・ルピー", 1.0),
        CurrencyType("PLN", "ポーランド・ズウォティ", 1.0),
        CurrencyType("PYG", "パラグアイ・グアラニー", 1.0),
        CurrencyType("QAR", "カタール・リヤル", 3.64),
        CurrencyType("RON", "ルーマニア・レウ", 1.0),
        CurrencyType("RSD", "セルビア・ディナール", 1.0),
        CurrencyType("RUB", "ロシア・ルーブル", 1.0),
        CurrencyType("RWF", "ルワンダ・フラン", 1.0),
        CurrencyType("SAR", "サウジアラビア・リヤル", 3.75),
        CurrencyType("SBD", "ソロモン諸島・ドル", 1.0),
        CurrencyType("SCR", "セーシェル・ルピー", 1.0),
        CurrencyType("SDG", "スーダン・ポンド", 1.0),
        CurrencyType("SEK", "スウェーデン・クローナ", 1.0),
        CurrencyType("SGD", "シンガポール・ドル", 1.0),
        CurrencyType("SOS", "ソマリア・シリング", 1.0),
        CurrencyType("SRD", "スリナム・ドル", 1.0),
        CurrencyType("SVC", "サルバドール・コロン", 1.0),
        CurrencyType("SZL", "スワジランド・リランゲニ", 1.0),
        CurrencyType("THB", "タイ・バーツ", 1.0),
        CurrencyType("TJS", "タジキスタン・ソモニ", 1.0),
        CurrencyType("TMT", "トルクメニスタン・マナト", 1.0),
        CurrencyType("TND", "チュニジア・ディナール", 1.0),
        CurrencyType("TOP", "トンガ・パアンガ", 1.0),
        CurrencyType("TRY", "トルコ・リラ", 1.0),
        CurrencyType("TTD", "トリニダード・トバゴ・ドル", 1.0),
        CurrencyType("TWD", "台湾・ドル", 1.0),
        CurrencyType("TZS", "タンザニア・シリング", 1.0),
        CurrencyType("UAH", "ウクライナ・フリヴニャ", 1.0),
        CurrencyType("UGX", "ウガンダ・シリング", 1.0),
        CurrencyType("UYU", "ウルグアイ・ペソ", 1.0),
        CurrencyType("VES", "ベネズエラ・ボリバル・ソベラノ", 1.0),
        CurrencyType("VND", "ベトナム・ドン", 1.0),
        CurrencyType("XCD", "東カリブ・ドル", 1.0),
        CurrencyType("XOF", "西アフリカCFAフラン", 1.0),
        CurrencyType("XPF", "CFPフラン", 1.0),
        CurrencyType("YER", "イエメン・リヤル", 1.0),
        CurrencyType("ZAR", "南アフリカ・ランド", 1.0),
        CurrencyType("ZMW", "ザンビア・クワチャ", 1.0)
    )

    init {
        // 既存と重複しないものを追加
        ADDITIONAL.forEach { add ->
            if (CURRENCIES.none { it.code == add.code }) CURRENCIES.add(add)
        }
        // コード順に整列
        CURRENCIES.sortBy { it.code }
    }
}

/**
 * Repository-like simple updater using a public free API (exchangerate.host)
 * Only symbols we care about are requested. Fallback gracefully.
 */
object CurrencyRateUpdater {
    private val client = okhttp3.OkHttpClient()
    private const val BASE_URL = "https://api.exchangerate.host/latest"
    private const val FALLBACK_URL = "https://open.er-api.com/v6/latest/USD" // fallback (full rates, no symbols filtering)
    @Volatile var lastErrorMessage: String? = null
    @Volatile var lastBodySnippet: String? = null

    /**
     * Fetch rates (base USD) and update CurrencyDefinitions.
     * Returns map or null on failure.
     */
    fun fetchAndUpdate(targetCodes: List<String> = CurrencyDefinitions.CURRENCIES.map { it.code }): Map<String, Double>? {
        lastErrorMessage = null
        lastBodySnippet = null
        // 1) メイン API
        val direct = fetchPrimary(targetCodes)
        if (direct != null) return direct
        // 2) フォールバック API (全レート取得後フィルタ)
        val fb = fetchFallback(targetCodes)
        if (fb != null) return fb
        if (lastErrorMessage == null) lastErrorMessage = "取得失敗(不明)"
        return null
    }

    private fun fetchPrimary(targetCodes: List<String>): Map<String, Double>? {
        val symbols = targetCodes.joinToString(",")
        val url = "$BASE_URL?base=USD&symbols=$symbols"
        val req = okhttp3.Request.Builder().url(url).get().build()
        return try {
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    lastErrorMessage = "Primary HTTP ${resp.code}"
                    return null
                }
                val body = resp.body?.string()
                if (body.isNullOrBlank()) {
                    lastErrorMessage = "Primary 空レスポンス"
                    return null
                }
                lastBodySnippet = body.take(180)
                // もし HTML を受け取っていれば中断
                if (body.trimStart().startsWith("<")) {
                    lastErrorMessage = "Primary HTML応答(リダイレクト/ブロック?)"
                    return null
                }
                val json = JSONObject(body)
                val ratesObj = json.optJSONObject("rates") ?: run {
                    lastErrorMessage = "rates欠落(Primary)"
                    return null
                }
                val map = mutableMapOf<String, Double>()
                targetCodes.forEach { c ->
                    val v = ratesObj.optDouble(c, Double.NaN)
                    if (!v.isNaN()) map[c] = v
                }
                if (map.isEmpty()) {
                    lastErrorMessage = "対象通貨不在(Primary)"
                    return null
                }
                CurrencyDefinitions.updateRates(map)
                map
            }
        } catch (e: Exception) {
            lastErrorMessage = "Primary例外:${e.javaClass.simpleName}"
            lastBodySnippet = e.message
            Log.w("CurrencyRateUpdater", "primary fetch failed", e)
            null
        }
    }

    private fun fetchFallback(targetCodes: List<String>): Map<String, Double>? {
        val req = okhttp3.Request.Builder().url(FALLBACK_URL).get().build()
        return try {
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    lastErrorMessage = (lastErrorMessage?.plus("; ") ?: "") + "Fallback HTTP ${resp.code}"
                    return null
                }
                val body = resp.body?.string()
                if (body.isNullOrBlank()) {
                    lastErrorMessage = (lastErrorMessage?.plus("; ") ?: "") + "Fallback 空レスポンス"
                    return null
                }
                lastBodySnippet = body.take(180)
                if (body.trimStart().startsWith("<")) {
                    lastErrorMessage = (lastErrorMessage?.plus("; ") ?: "") + "Fallback HTML応答"
                    return null
                }
                val json = JSONObject(body)
                val ratesObj = json.optJSONObject("rates")
                    ?: json.optJSONObject("conversion_rates")
                    ?: run {
                        lastErrorMessage = (lastErrorMessage?.plus("; ") ?: "") + "rates欠落(Fallback)"
                        return null
                    }
                val map = mutableMapOf<String, Double>()
                targetCodes.forEach { c ->
                    val v = ratesObj.optDouble(c, Double.NaN)
                    if (!v.isNaN()) map[c] = v
                }
                if (map.isEmpty()) {
                    lastErrorMessage = (lastErrorMessage?.plus("; ") ?: "") + "対象通貨不在(Fallback)"
                    return null
                }
                CurrencyDefinitions.updateRates(map)
                // 成功したのでスニペットはユーザー表示不要のためクリア
                lastBodySnippet = null
                map
            }
        } catch (e: Exception) {
            lastErrorMessage = (lastErrorMessage?.plus("; ") ?: "") + "Fallback例外:${e.javaClass.simpleName}"
            if (lastBodySnippet == null) lastBodySnippet = e.message
            Log.w("CurrencyRateUpdater", "fallback fetch failed", e)
            null
        }
    }
}

class CurrencyConverter {
    /**
     * from -> to へ換算
     */
    fun convert(amount: Double, from: CurrencyType, to: CurrencyType): Double {
        if (amount == 0.0) return 0.0
        // まず USD に戻す: amount_in_USD = amount / ratePerUSD(from)
        val amountInUSD = amount / from.ratePerUSD
        // 目標通貨: amount_in_to = amount_in_USD * ratePerUSD(to)
        return amountInUSD * to.ratePerUSD
    }

    fun format(value: Double): String {
        return when {
            value == 0.0 -> "0"
            value.isNaN() -> "エラー"
            value.isInfinite() -> if (value > 0) "∞" else "-∞"
            kotlin.math.abs(value) >= 1e15 || (kotlin.math.abs(value) < 1e-6 && value != 0.0) -> String.format("%.4e", value)
            else -> {
                val bd = BigDecimal(value).setScale(8, RoundingMode.HALF_UP).stripTrailingZeros()
                addCommas(bd.toPlainString())
            }
        }
    }

    private fun addCommas(num: String): String {
        val parts = num.split('.')
        val intPart = parts[0]
        val decPart = parts.getOrNull(1)
        val formattedInt = intPart.reversed().chunked(3).joinToString(",").reversed()
        return if (decPart != null && decPart.isNotEmpty()) "$formattedInt.$decPart" else formattedInt
    }
}
