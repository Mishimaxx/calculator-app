package com.example.test2.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.test2.ui.viewmodel.CalculatorViewModel

@Composable
fun ScientificKeypad(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    var isSecondFunctionActive by remember { mutableStateOf(false) }

    val activeColor = Color(0xFFFFA500)
    val inactiveColor = Color(0xFFA5A5A5)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1st Row: 2nd, x², x³, x^y, 1/x
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalculatorButton(
                text = "2nd",
                onClick = { isSecondFunctionActive = !isSecondFunctionActive },
                modifier = Modifier.weight(1f),
                backgroundColor = if (isSecondFunctionActive) activeColor else inactiveColor,
                textColor = if (isSecondFunctionActive) Color.White else Color.Black
            )
            CalculatorButton(
                text = if (isSecondFunctionActive) "x³" else "x²",
                onClick = { viewModel.onScientificFunctionClicked(if (isSecondFunctionActive) "^3" else "^2") },
                modifier = Modifier.weight(1f),
                isSpecial = true
            )
            CalculatorButton(
                text = if (isSecondFunctionActive) "³√x" else "√x",
                onClick = { viewModel.onScientificFunctionClicked(if (isSecondFunctionActive) "cbrt" else "sqrt") },
                modifier = Modifier.weight(1f),
                isSpecial = true
            )
            CalculatorButton(
                text = "x^y",
                onClick = { viewModel.onScientificFunctionClicked("^") },
                modifier = Modifier.weight(1f),
                isSpecial = true
            )
            CalculatorButton(
                text = "1/x",
                onClick = { viewModel.onScientificFunctionClicked("1/x") },
                modifier = Modifier.weight(1f),
                isSpecial = true
            )
        }

        // 2nd Row: sin, cos, tan, log, ln
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalculatorButton(
                text = if (isSecondFunctionActive) "sin⁻¹" else "sin",
                onClick = { viewModel.onScientificFunctionClicked(if (isSecondFunctionActive) "asin" else "sin") },
                modifier = Modifier.weight(1f),
                isSpecial = true
            )
            CalculatorButton(
                text = if (isSecondFunctionActive) "cos⁻¹" else "cos",
                onClick = { viewModel.onScientificFunctionClicked(if (isSecondFunctionActive) "acos" else "cos") },
                modifier = Modifier.weight(1f),
                isSpecial = true
            )
            CalculatorButton(
                text = if (isSecondFunctionActive) "tan⁻¹" else "tan",
                onClick = { viewModel.onScientificFunctionClicked(if (isSecondFunctionActive) "atan" else "tan") },
                modifier = Modifier.weight(1f),
                isSpecial = true
            )
            CalculatorButton(
                text = "log",
                onClick = { viewModel.onScientificFunctionClicked("log") },
                modifier = Modifier.weight(1f),
                isSpecial = true
            )
            CalculatorButton(
                text = "ln",
                onClick = { viewModel.onScientificFunctionClicked("ln") },
                modifier = Modifier.weight(1f),
                isSpecial = true
            )
        }

        // 3rd Row: AC, ⌫, %, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalculatorButton(
                text = "AC",
                onClick = { viewModel.onClearClicked() },
                modifier = Modifier.weight(1f),
                isSpecial = true
            )
            CalculatorButton(
                text = "⌫",
                onClick = { viewModel.onBackspaceClicked() },
                modifier = Modifier.weight(1f),
                isSpecial = true
            )
            CalculatorButton(
                text = "%",
                onClick = { viewModel.onOperatorClicked("%") },
                modifier = Modifier.weight(1f),
                isOperator = true
            )
            CalculatorButton(
                text = "÷",
                onClick = { viewModel.onOperatorClicked("/") },
                modifier = Modifier.weight(1f),
                isOperator = true
            )
        }

        // Keypad (same as BasicKeypad)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalculatorButton("7", { viewModel.onNumberClicked("7") }, Modifier.weight(1f))
            CalculatorButton("8", { viewModel.onNumberClicked("8") }, Modifier.weight(1f))
            CalculatorButton("9", { viewModel.onNumberClicked("9") }, Modifier.weight(1f))
            CalculatorButton("×", { viewModel.onOperatorClicked("*") }, Modifier.weight(1f), isOperator = true)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalculatorButton("4", { viewModel.onNumberClicked("4") }, Modifier.weight(1f))
            CalculatorButton("5", { viewModel.onNumberClicked("5") }, Modifier.weight(1f))
            CalculatorButton("6", { viewModel.onNumberClicked("6") }, Modifier.weight(1f))
            CalculatorButton("-", { viewModel.onOperatorClicked("-") }, Modifier.weight(1f), isOperator = true)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalculatorButton("1", { viewModel.onNumberClicked("1") }, Modifier.weight(1f))
            CalculatorButton("2", { viewModel.onNumberClicked("2") }, Modifier.weight(1f))
            CalculatorButton("3", { viewModel.onNumberClicked("3") }, Modifier.weight(1f))
            CalculatorButton("+", { viewModel.onOperatorClicked("+") }, Modifier.weight(1f), isOperator = true)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WideCalculatorButton("0", { viewModel.onNumberClicked("0") }, Modifier.weight(2f))
            CalculatorButton(".", { viewModel.onDecimalClicked() }, Modifier.weight(1f))
            CalculatorButton("=", { viewModel.onEqualsClicked() }, Modifier.weight(1f), isEquals = true)
        }
    }
}
