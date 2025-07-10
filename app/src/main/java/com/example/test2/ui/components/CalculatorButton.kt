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

/**
 * 電卓のボタンコンポーネント
 */
@Composable
fun CalculatorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF505050), // グレー（数字ボタン用）
    textColor: Color = Color.White,
    isOperator: Boolean = false,
    isSpecial: Boolean = false,
    isEquals: Boolean = false
) {
    val buttonColor = Color(0xFF505050) // 全てのボタンを数字ボタンと同じグレー背景
    
    val contentColor = when {
        isEquals -> Color(0xFFFF6B6B) // =ボタンは赤文字
        isSpecial -> Color(0xFFFF6B6B) // C、()、%、⌫は赤文字
        isOperator -> Color(0xFFFF6B6B) // 演算子は赤文字
        else -> textColor // 数字ボタンは白文字
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .height(68.dp) // 55dpから68dpに増加してボタンを大きく
            .padding(1.dp), // パディングは最小に
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 1.dp,
            pressedElevation = 2.dp // 4dpから2dpに縮小
        )
    ) {
        Text(
            text = text,
            fontSize = 32.sp, // 24spから32spに大幅増加
            fontWeight = FontWeight.Bold, // Lightから携帯フォントらしいBoldに変更
            textAlign = TextAlign.Center
        )
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
    backgroundColor: Color = Color(0xFF505050), // 数字ボタンと同じ背景色
    textColor: Color = Color(0xFFFF6B6B) // 赤文字（=ボタン用）
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(60.dp) // 64dpから60dpに変更
            .padding(2.dp), // 4dpから2dpに縮小
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        shape = RoundedCornerShape(12.dp), // 16dpから12dpに縮小
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp, // 4dpから2dpに縮小
            pressedElevation = 4.dp // 8dpから4dpに縮小
        )
    ) {
        Text(
            text = text,
            fontSize = 32.sp, // 24spから32spに大幅増加
            fontWeight = FontWeight.Bold // Lightから携帯フォントらしいBoldに変更
        )
    }
}

@Preview(showBackground = true)
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
                    text = "7",
                    onClick = { },
                    modifier = Modifier.weight(1f)
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
