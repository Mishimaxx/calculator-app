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
 * 電卓の表示画面コンポーネント（CASIO風液晶ディスプレイ）
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

    // ダークなグレーの背景
    val displayBackground = Color(0xFF404040) // ダークなグレーの背景
    val textColor = Color.White // 白い文字色
    val resultColor = Color(0xFFFF6B6B) // ボタンと同じ赤色

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(displayBackground)
            .padding(16.dp)
    ) {
        // メインディスプレイエリア - 下の方に配置
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom
        ) {
            // 上側は空白スペース
            Spacer(modifier = Modifier.weight(1f))

            // 式表示部分
            if (expression.isNotEmpty()) {
                val formattedExpression = formatExpressionSpacing(expression)
                // 式入力時は式を大きく強調、結果表示時は小さく
                val expressionFontSize = calculateFontSize(formattedExpression, if (isResultShowing) 18f else 32f, 30)
                Text(
                    text = formattedExpression,
                    fontSize = expressionFontSize.sp,
                    color = textColor,
                    textAlign = TextAlign.End,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = FontFamily.Monospace,
                    // 式入力時は太字で強調
                    fontWeight = if (isResultShowing) FontWeight.Normal else FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    lineHeight = (expressionFontSize * 0.9f).sp
                )
            }

            // 結果表示
            if (previewResult.isNotEmpty() || isResultShowing || displayText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val resultText = if (isResultShowing) displayText else if (previewResult.isNotEmpty()) previewResult else displayText
                // = 押したら結果を大きく、式入力時は小さく
                val resultFontSize = calculateFontSize(resultText, if (isResultShowing) 40f else 20f, 20)
                
                Text(
                    text = if (!isResultShowing && previewResult.isNotEmpty()) "= $resultText" else resultText,
                    fontSize = resultFontSize.sp,
                    // = 押したら赤文字で強調
                    color = if (isResultShowing) resultColor else textColor,
                    textAlign = TextAlign.End,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = FontFamily.Monospace,
                    // = 押したら太字で強調
                    fontWeight = if (isResultShowing) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.fillMaxWidth(),
                    lineHeight = (resultFontSize * 0.9f).sp
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
            displayText = "1",
            expression = "sin(π/2)+cos(π/2)",
            previewResult = "",
            isResultShowing = true
        )
    }
}
