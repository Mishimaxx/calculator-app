package com.example.test2.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.test2.ui.viewmodel.CalculatorViewModel

/**
 * 単位換算用の軽量キーパッド（画像レイアウト準拠）
 */
@Composable
fun UnitConversionKeypad(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier,
    onRequestOpenBasic: (() -> Unit)? = null,
    onSwapUnits: (() -> Unit)? = null
) {
    // BasicKeypad の配色と間隔に合わせる（4dp 間隔、デフォルト配色）
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 1行目: 7 8 9 ⌫（⌫は赤文字・特別扱い）
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CalculatorButton("7", { viewModel.onNumberClicked("7") }, Modifier.weight(1f))
            CalculatorButton("8", { viewModel.onNumberClicked("8") }, Modifier.weight(1f))
            CalculatorButton("9", { viewModel.onNumberClicked("9") }, Modifier.weight(1f))
            CalculatorButton(
                text = "⌫",
                onClick = { viewModel.onBackspaceClicked() },
                modifier = Modifier.weight(1f),
                isSpecial = true
            )
        }

        // 2行目: 4 5 6 C（Cは赤文字・特別扱い）
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CalculatorButton("4", { viewModel.onNumberClicked("4") }, Modifier.weight(1f))
            CalculatorButton("5", { viewModel.onNumberClicked("5") }, Modifier.weight(1f))
            CalculatorButton("6", { viewModel.onNumberClicked("6") }, Modifier.weight(1f))
            CalculatorButton(
                text = "C",
                onClick = { viewModel.onClearClicked() },
                modifier = Modifier.weight(1f),
                isSpecial = true
            )
        }

        // 3行目: 1 2 3 [演算子集合（赤文字扱い）]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CalculatorButton("1", { viewModel.onNumberClicked("1") }, Modifier.weight(1f))
            CalculatorButton("2", { viewModel.onNumberClicked("2") }, Modifier.weight(1f))
            CalculatorButton("3", { viewModel.onNumberClicked("3") }, Modifier.weight(1f))
            CalculatorButton(
                text = "+  −\n×  =",
                onClick = { onRequestOpenBasic?.invoke() },
                modifier = Modifier.weight(1f),
                isOperator = true,
                fontWeight = FontWeight.SemiBold
            )
        }

        // 4行目: 00 0 . [1↕（通常配色に変更）]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CalculatorButton("00", { viewModel.onNumberClicked("00") }, Modifier.weight(1f))
            CalculatorButton("0", { viewModel.onNumberClicked("0") }, Modifier.weight(1f))
            CalculatorButton(".", { viewModel.onDecimalClicked() }, Modifier.weight(1f))
            CalculatorButton(
                text = "1↕",
                onClick = { onSwapUnits?.invoke() },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
