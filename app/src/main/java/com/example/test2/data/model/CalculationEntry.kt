package com.example.test2.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 計算履歴のデータモデル
 */
@Entity(tableName = "calculation_entries")
data class CalculationEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val expression: String,
    val result: String,
    val timestamp: Date,
    val calculationType: CalculationType = CalculationType.BASIC
)

enum class CalculationType {
    BASIC,      // 基本四則演算
    SCIENTIFIC, // 科学計算（sin, cos, log等）
    PROGRAMMER  // プログラマ向け（進数変換、ビット演算等）
}
