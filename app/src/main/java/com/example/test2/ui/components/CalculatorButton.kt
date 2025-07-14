package com.example.test2.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.text.style.TextOverflow

/**
 * 電卓のボタンコンポーネント
 */
@Composable
fun CalculatorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF333333), // 数字ボタン用の濃いグレー
    textColor: Color = Color.White,
    isOperator: Boolean = false,
    isSpecial: Boolean = false,
    isEquals: Boolean = false,
    fontSize: androidx.compose.ui.unit.TextUnit = 32.sp,
    height: androidx.compose.ui.unit.Dp = 68.dp,
    fontWeight: FontWeight = FontWeight.Normal,
    shiftFunction: String = "", // SHIFT時の機能
    alphaFunction: String = "", // ALPHA時の機能
    shiftColor: Color = Color(0xFFFFD700), // 黄色
    alphaColor: Color = Color(0xFF6B9DFF) // 青色
) {
    val buttonColor = when {
        isEquals -> Color(0xFFFFA500) // オレンジ
        isOperator -> Color(0xFFFFA500) // オレンジ
        isSpecial -> Color(0xFFA5A5A5) // 明るいグレー
        else -> backgroundColor
    }
    
    val contentColor = when {
        isSpecial -> Color.Black
        else -> textColor
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .height(height)
            .padding(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = null // 影をなくす
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (shiftFunction.isNotEmpty()) {
                Text(
                    text = shiftFunction,
                    fontSize = 10.sp,
                    color = shiftColor,
                    modifier = Modifier.align(Alignment.TopStart).padding(start = 4.dp, top = 4.dp),
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (alphaFunction.isNotEmpty()) {
                Text(
                    text = alphaFunction,
                    fontSize = 10.sp,
                    color = alphaColor,
                    modifier = Modifier.align(Alignment.TopEnd).padding(end = 4.dp, top = 4.dp),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val lines = text.split("\n")
                lines.forEach { line ->
                    Text(
                        text = line,
                        fontSize = if (lines.size > 1) fontSize * 0.8f else fontSize,
                        fontWeight = fontWeight,
                        textAlign = TextAlign.Center,
                        color = contentColor,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip
                    )
                }
            }
        }
    }
}

/**
 * ワイドなボタン（2列分の幅）
 */
@Composable
fun WideCalculatorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFFFA500), // オレンジ
    textColor: Color = Color.White
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(68.dp)
            .padding(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = null
    ) {
        Text(
            text = text,
            fontSize = 32.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1C1E)
@Composable
fun CalculatorButtonPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButton(
                    text = "sin",
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    isSpecial = true,
                    shiftFunction = "sin⁻¹",
                    alphaFunction = "D"
                )
                CalculatorButton(
                    text = "+",
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    isOperator = true
                )
                CalculatorButton(
                    text = "C",
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    isSpecial = true
                )
            }
            WideCalculatorButton(
                text = "=",
                onClick = { }
            )
        }
    }
}
