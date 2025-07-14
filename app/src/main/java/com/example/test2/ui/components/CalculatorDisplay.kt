package com.example.test2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val displayBackground = Color(0xFF222222)
    val textColor = Color.White
    val expressionColor = Color.Gray

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(displayBackground)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = expression,
                fontSize = 24.sp,
                color = expressionColor,
                textAlign = TextAlign.End,
                maxLines = 2,
                fontWeight = FontWeight.Light,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isResultShowing) displayText else previewResult.ifEmpty { displayText },
                fontSize = 48.sp,
                color = textColor,
                textAlign = TextAlign.End,
                maxLines = 1,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1C1E)
@Composable
fun CalculatorDisplayPreview() {
    MaterialTheme {
        CalculatorDisplay(
            displayText = "1,000",
            expression = "500+500",
            previewResult = "1,000",
            isResultShowing = true
        )
    }
}
