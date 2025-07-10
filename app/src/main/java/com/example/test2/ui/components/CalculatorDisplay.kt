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
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E)) // ダークグレー背景
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 式表示部分（常に表示）
            if (expression.isNotEmpty()) {
                Text(
                    text = expression,
                    fontSize = if (isResultShowing) 20.sp else 28.sp,
                    color = Color.White, // 白文字
                    textAlign = TextAlign.End,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // プレビュー結果表示（計算途中または結果表示時）
            if (previewResult.isNotEmpty() || isResultShowing) {
                Text(
                    text = if (isResultShowing) "= $displayText" else "= $previewResult",
                    fontSize = 24.sp,
                    color = Color.White, // 白文字
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
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
