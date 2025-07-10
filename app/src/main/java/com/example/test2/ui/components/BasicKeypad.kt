package com.example.test2.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.example.test2.ui.viewmodel.CalculatorViewModel

/**
 * 基本的な電卓キーパッド
 */
@Composable
fun BasicKeypad(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1行目: C, (), %, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalculatorButton(
                text = "C",
                onClick = { viewModel.onClearClicked() },
                modifier = Modifier.weight(1f),
                isSpecial = true
            )
            CalculatorButton(
                text = "()",
                onClick = { viewModel.onParenthesesClicked() },
                modifier = Modifier.weight(1f),
                isSpecial = true
            )
            CalculatorButton(
                text = "%",
                onClick = { viewModel.onOperatorClicked("%") },
                modifier = Modifier.weight(1f),
                isSpecial = true
            )
            CalculatorButton(
                text = "÷",
                onClick = { viewModel.onOperatorClicked("/") },
                modifier = Modifier.weight(1f),
                isOperator = true
            )
        }

        // 2行目: 7, 8, 9, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalculatorButton(
                text = "7",
                onClick = { viewModel.onNumberClicked("7") },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "8",
                onClick = { viewModel.onNumberClicked("8") },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "9",
                onClick = { viewModel.onNumberClicked("9") },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "×",
                onClick = { viewModel.onOperatorClicked("*") },
                modifier = Modifier.weight(1f),
                isOperator = true
            )
        }

        // 3行目: 4, 5, 6, -
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalculatorButton(
                text = "4",
                onClick = { viewModel.onNumberClicked("4") },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "5",
                onClick = { viewModel.onNumberClicked("5") },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "6",
                onClick = { viewModel.onNumberClicked("6") },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "−",
                onClick = { viewModel.onOperatorClicked("-") },
                modifier = Modifier.weight(1f),
                isOperator = true
            )
        }

        // 4行目: 1, 2, 3, +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalculatorButton(
                text = "1",
                onClick = { viewModel.onNumberClicked("1") },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "2",
                onClick = { viewModel.onNumberClicked("2") },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "3",
                onClick = { viewModel.onNumberClicked("3") },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "+",
                onClick = { viewModel.onOperatorClicked("+") },
                modifier = Modifier.weight(1f),
                isOperator = true
            )
        }

        // 5行目: 0, ., ⌫, =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalculatorButton(
                text = "0",
                onClick = { viewModel.onNumberClicked("0") },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = ".",
                onClick = { viewModel.onDecimalClicked() },
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "⌫",
                onClick = { viewModel.onBackspaceClicked() },
                modifier = Modifier.weight(1f),
                isSpecial = true
            )
            CalculatorButton(
                text = "=",
                onClick = { viewModel.onEqualsClicked() },
                modifier = Modifier.weight(1f),
                isEquals = true
            )
        }
    }
}
