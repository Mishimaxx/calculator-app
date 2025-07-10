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
    val buttonColor = when {
        isEquals -> Color(0xFF6B46C1) // 紫色（=ボタン用）
        isSpecial -> Color(0xFFA5A5A5) // 薄いグレー（C、()、%、⌫用）
        isOperator -> Color(0xFFDC2626) // 赤色（演算子用）
        else -> backgroundColor // グレー（数字用）
    }
    
    val contentColor = when {
        isEquals -> Color.White
        isSpecial -> Color.Black
        isOperator -> Color.White
        else -> textColor
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1f)
            .padding(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
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
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
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
