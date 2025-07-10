package com.example.test2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min

/**
 * 電卓の表示画面コンポーネント
 */
@Composable
fun CalculatorDisplay(
    displayText: String,
    expression: String,
    previewResult: String = "",
    isResultShowing: Boolean = false,
    modifier: Modifier = Modifier
) {
    // 文字数に応じて動的にフォントサイズを調整する関数
    fun calculateFontSize(text: String, baseFontSize: Float, maxLength: Int = 20): Float {
        return when {
            text.length <= maxLength / 2 -> baseFontSize
            text.length <= maxLength -> baseFontSize * 0.8f
            text.length <= maxLength * 1.5 -> baseFontSize * 0.65f
            else -> baseFontSize * 0.45f // より小さくして長い式に対応
        }
    }

    // 演算子の間隔を調整する関数
    fun formatExpressionSpacing(expression: String): String {
        return expression
            .replace(" + ", "+")
            .replace(" - ", "-") 
            .replace(" × ", "×")
            .replace(" ÷ ", "÷")
            .replace(" * ", "×")
            .replace(" / ", "÷")
            // スペースなしで直接つなげる（より狭い間隔）
            .replace("+", "+")
            .replace("-", "-")
            .replace("×", "×")
            .replace("÷", "÷")
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(0.dp), // 全てのパディングを削除して横端まで広げる
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // 影を削除
        shape = RoundedCornerShape(0.dp), // 角を完全に四角に
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E)) // ダークグレー背景
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp), // パディングを縮小
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom // 下揃えに変更
        ) {
            // 上側は空白スペース（画面を広く使う）
            Spacer(modifier = Modifier.weight(1f))

            // 式表示部分（一番下に配置）
            if (expression.isNotEmpty()) {
                val formattedExpression = formatExpressionSpacing(expression)
                val expressionFontSize = calculateFontSize(formattedExpression, if (isResultShowing) 22f else 30f, 30) // より大きく
                Text(
                    text = formattedExpression,
                    fontSize = expressionFontSize.sp,
                    color = if (isResultShowing) Color(0xFF808080) else Color.White, // =押す前は白で強調、押した後は薄いグレー
                    textAlign = TextAlign.End,
                    maxLines = 2, // 最大2行に制限して答えが消えないように
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (isResultShowing) FontWeight.Normal else FontWeight.Bold, // =押す前は太字で強調
                    modifier = Modifier.fillMaxWidth(),
                    lineHeight = (expressionFontSize * 0.9f).sp // 行間をさらに狭く
                )
            }

            // プレビュー結果表示（途中の答えまたは最終結果）
            if (previewResult.isNotEmpty() || isResultShowing || displayText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp)) // 間隔を少し広げる
                val resultText = if (isResultShowing) displayText else if (previewResult.isNotEmpty()) previewResult else displayText
                val resultFontSize = calculateFontSize(resultText, if (isResultShowing) 36f else 26f, 20) // より大きく
                Text(
                    text = "=$resultText", // スペースを削除してより密接に
                    fontSize = resultFontSize.sp,
                    color = if (isResultShowing) Color(0xFFFF6B6B) else Color(0xFFA0A0A0), // =押した後は赤色、途中はグレー
                    textAlign = TextAlign.End,
                    maxLines = 2, // 最大2行まで表示
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (isResultShowing) FontWeight.Bold else FontWeight.Normal, // =押した後は太字で強調
                    modifier = Modifier.fillMaxWidth(),
                    lineHeight = (resultFontSize * 0.9f).sp // 行間をさらに狭く
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalculatorDisplayPreview() {
    MaterialTheme {
        CalculatorDisplay(
            displayText = "99",
            expression = "99+99",
            previewResult = "198",
            isResultShowing = false
        )
    }
}
