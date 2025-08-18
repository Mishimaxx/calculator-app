package com.example.test2.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.ui.viewmodel.CalculatorViewModel

/**
 * CASIO関数電卓風のキーパッド（fx-991ESPLUSに基づく）
 */
@Composable
fun CasioScientificKeypad(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    var showSecondFunction by remember { mutableStateOf(false) }
    var isDegreeMode by remember { mutableStateOf(true) } // 度数法/ラジアン切り替え
    
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // 1行目: SHIFT, ALPHA, REPLAY, MODE, ON
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CalculatorButton(
                text = "SHIFT",
                onClick = { showSecondFunction = !showSecondFunction },
                modifier = Modifier.weight(1f),
                isSpecial = showSecondFunction,
                fontSize = 10.sp,
                height = 40.dp
            )
            CalculatorButton(
                text = "ALPHA",
                onClick = { /* アルファ機能は省略 */ },
                modifier = Modifier.weight(1f),
                isSpecial = false,
                fontSize = 10.sp,
                height = 40.dp
            )
            CalculatorButton(
                text = "REPLAY",
                onClick = { /* リプレイ機能は省略 */ },
                modifier = Modifier.weight(1f),
                isSpecial = false,
                fontSize = 9.sp,
                height = 40.dp
            )
            CalculatorButton(
                text = "MODE",
                onClick = { isDegreeMode = !isDegreeMode },
                modifier = Modifier.weight(1f),
                isSpecial = false,
                fontSize = 10.sp,
                height = 40.dp
            )
            CalculatorButton(
                text = "ON",
                onClick = { viewModel.onClearClicked() },
                modifier = Modifier.weight(1f),
                isSpecial = false,
                fontSize = 10.sp,
                height = 40.dp
            )
        }

        // 2行目: xy, log, ln, (-), DEL
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // 一般べき乗 x^y
            CalculatorButton(
                text = "xʸ",
                onClick = {
                    viewModel.onScientificFunctionClicked("^")
                    showSecondFunction = false
                },
                modifier = Modifier.weight(1f),
                isOperator = true,
                fontSize = 12.sp,
                height = 45.dp
            )
            CalculatorButton(
                text = if (showSecondFunction) "10x" else "log",
                onClick = { 
                    if (showSecondFunction) {
                        viewModel.onScientificFunctionClicked("10^x")
                    } else {
                        viewModel.onScientificFunctionClicked("log")
                    }
                    showSecondFunction = false
                },
                modifier = Modifier.weight(1f),
                isOperator = true,
                fontSize = 12.sp,
                height = 45.dp
            )
            CalculatorButton(
                text = if (showSecondFunction) "ex" else "ln",
                onClick = { 
                    if (showSecondFunction) {
                        viewModel.onScientificFunctionClicked("e^x")
                    } else {
                        viewModel.onScientificFunctionClicked("ln")
                    }
                    showSecondFunction = false
                },
                modifier = Modifier.weight(1f),
                isOperator = true,
                fontSize = 12.sp,
                height = 45.dp
            )
            CalculatorButton(
                text = "(-)",
                onClick = { viewModel.onOperatorClicked("-") },
                modifier = Modifier.weight(1f),
                isOperator = true,
                fontSize = 14.sp,
                height = 45.dp
            )
            CalculatorButton(
                text = "DEL",
                onClick = { viewModel.onBackspaceClicked() },
                modifier = Modifier.weight(1f),
                isOperator = true,
                fontSize = 11.sp,
                height = 45.dp
            )
        }

        // 3行目: sin, cos, tan, ÷, AC
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CalculatorButton(
                text = if (showSecondFunction) "sin⁻¹" else "sin",
                onClick = { 
                    if (showSecondFunction) {
                        viewModel.onScientificFunctionClicked("asin")
                    } else {
                        viewModel.onScientificFunctionClicked("sin")
                    }
                    showSecondFunction = false
                },
                modifier = Modifier.weight(1f),
                isOperator = true,
                fontSize = 12.sp,
                height = 50.dp
            )
            CalculatorButton(
                text = if (showSecondFunction) "cos⁻¹" else "cos",
                onClick = { 
                    if (showSecondFunction) {
                        viewModel.onScientificFunctionClicked("acos")
                    } else {
                        viewModel.onScientificFunctionClicked("cos")
                    }
                    showSecondFunction = false
                },
                modifier = Modifier.weight(1f),
                isOperator = true,
                fontSize = 12.sp,
                height = 50.dp
            )
            CalculatorButton(
                text = if (showSecondFunction) "tan⁻¹" else "tan",
                onClick = { 
                    if (showSecondFunction) {
                        viewModel.onScientificFunctionClicked("atan")
                    } else {
                        viewModel.onScientificFunctionClicked("tan")
                    }
                    showSecondFunction = false
                },
                modifier = Modifier.weight(1f),
                isOperator = true,
                fontSize = 12.sp,
                height = 50.dp
            )
            CalculatorButton(
                text = "÷",
                onClick = { viewModel.onOperatorClicked("/") },
                modifier = Modifier.weight(1f),
                isOperator = true,
                fontSize = 18.sp,
                height = 50.dp
            )
            CalculatorButton(
                text = "AC",
                onClick = { viewModel.onClearClicked() },
                modifier = Modifier.weight(1f),
                isSpecial = true,
                fontSize = 12.sp,
                height = 50.dp
            )
        }

        // 4行目: x², x⁻¹, √, ×, 進数切り替え
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CalculatorButton(
                text = if (showSecondFunction) "x³" else "x²",
                onClick = { 
                    if (showSecondFunction) {
                        viewModel.onScientificFunctionClicked("x³")
                    } else {
                        viewModel.onScientificFunctionClicked("x²")
                    }
                    showSecondFunction = false
                },
                modifier = Modifier.weight(1f),
                isOperator = true,
                fontSize = 12.sp,
                height = 50.dp
            )
            CalculatorButton(
                text = if (showSecondFunction) "∛" else "x⁻¹",
                onClick = { 
                    if (showSecondFunction) {
                        viewModel.onScientificFunctionClicked("∛")
                    } else {
                        viewModel.onScientificFunctionClicked("1/x")
                    }
                    showSecondFunction = false
                },
                modifier = Modifier.weight(1f),
                isOperator = true,
                fontSize = 12.sp,
                height = 50.dp
            )
            CalculatorButton(
                text = "√",
                onClick = { 
                    viewModel.onScientificFunctionClicked("sqrt")
                    showSecondFunction = false
                },
                modifier = Modifier.weight(1f),
                isOperator = true,
                fontSize = 16.sp,
                height = 50.dp
            )
            CalculatorButton(
                text = "×",
                onClick = { viewModel.onOperatorClicked("*") },
                modifier = Modifier.weight(1f),
                isOperator = true,
                fontSize = 18.sp,
                height = 50.dp
            )
            CalculatorButton(
                text = if (showSecondFunction) "HEX" else "DEC",
                onClick = { 
                    if (showSecondFunction) {
                        viewModel.onBaseConversionClicked("HEX")
                    } else {
                        viewModel.onBaseConversionClicked("DEC")
                    }
                    showSecondFunction = false
                },
                modifier = Modifier.weight(1f),
                isSpecial = true,
                fontSize = 10.sp,
                height = 50.dp
            )
        }

        // 5行目: (, ), S⇔D, -, 進数切り替え2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CalculatorButton(
                text = "(",
                onClick = { viewModel.onNumberClicked("(") },
                modifier = Modifier.weight(1f),
                isOperator = true,
                fontSize = 18.sp,
                height = 50.dp
            )
            CalculatorButton(
                text = ")",
                onClick = { viewModel.onNumberClicked(")") },
                modifier = Modifier.weight(1f),
                isOperator = true,
                fontSize = 18.sp,
                height = 50.dp
            )
            CalculatorButton(
                text = if (isDegreeMode) "D" else "R",
                onClick = { isDegreeMode = !isDegreeMode },
                modifier = Modifier.weight(1f),
                isSpecial = true,
                fontSize = 14.sp,
                height = 50.dp
            )
            CalculatorButton(
                text = "-",
                onClick = { viewModel.onOperatorClicked("-") },
                modifier = Modifier.weight(1f),
                isOperator = true,
                fontSize = 18.sp,
                height = 50.dp
            )
            CalculatorButton(
                text = if (showSecondFunction) "BIN" else "OCT",
                onClick = { 
                    if (showSecondFunction) {
                        viewModel.onBaseConversionClicked("BIN")
                    } else {
                        viewModel.onBaseConversionClicked("OCT")
                    }
                    showSecondFunction = false
                },
                modifier = Modifier.weight(1f),
                isSpecial = true,
                fontSize = 10.sp,
                height = 50.dp
            )
        }

        // 6行目: 7, 8, 9, +, π/e
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CalculatorButton(
                text = "7",
                onClick = { viewModel.onNumberClicked("7") },
                modifier = Modifier.weight(1f),
                fontSize = 20.sp,
                height = 55.dp
            )
            CalculatorButton(
                text = "8",
                onClick = { viewModel.onNumberClicked("8") },
                modifier = Modifier.weight(1f),
                fontSize = 20.sp,
                height = 55.dp
            )
            CalculatorButton(
                text = "9",
                onClick = { viewModel.onNumberClicked("9") },
                modifier = Modifier.weight(1f),
                fontSize = 20.sp,
                height = 55.dp
            )
            CalculatorButton(
                text = "+",
                onClick = { viewModel.onOperatorClicked("+") },
                modifier = Modifier.weight(1f),
                isOperator = true,
                fontSize = 18.sp,
                height = 55.dp
            )
            CalculatorButton(
                text = if (showSecondFunction) "e" else "π",
                onClick = { 
                    if (showSecondFunction) {
                        viewModel.onNumberClicked("2.71828")
                    } else {
                        viewModel.onNumberClicked("3.14159")
                    }
                    showSecondFunction = false
                },
                modifier = Modifier.weight(1f),
                isSpecial = true,
                fontSize = 14.sp,
                height = 55.dp
            )
        }

        // 7行目: 4, 5, 6, EXP, Ans
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CalculatorButton(
                text = "4",
                onClick = { viewModel.onNumberClicked("4") },
                modifier = Modifier.weight(1f),
                fontSize = 20.sp,
                height = 55.dp
            )
            CalculatorButton(
                text = "5",
                onClick = { viewModel.onNumberClicked("5") },
                modifier = Modifier.weight(1f),
                fontSize = 20.sp,
                height = 55.dp
            )
            CalculatorButton(
                text = "6",
                onClick = { viewModel.onNumberClicked("6") },
                modifier = Modifier.weight(1f),
                fontSize = 20.sp,
                height = 55.dp
            )
            CalculatorButton(
                text = "EXP",
                onClick = { viewModel.onScientificFunctionClicked("exp") },
                modifier = Modifier.weight(1f),
                isSpecial = true,
                fontSize = 12.sp,
                height = 55.dp
            )
            CalculatorButton(
                text = "Ans",
                onClick = { /* 前回の答えを入力 */ },
                modifier = Modifier.weight(1f),
                isSpecial = true,
                fontSize = 12.sp,
                height = 55.dp
            )
        }

        // 8行目: 1, 2, 3, =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CalculatorButton(
                text = "1",
                onClick = { viewModel.onNumberClicked("1") },
                modifier = Modifier.weight(1f),
                fontSize = 20.sp,
                height = 55.dp
            )
            CalculatorButton(
                text = "2",
                onClick = { viewModel.onNumberClicked("2") },
                modifier = Modifier.weight(1f),
                fontSize = 20.sp,
                height = 55.dp
            )
            CalculatorButton(
                text = "3",
                onClick = { viewModel.onNumberClicked("3") },
                modifier = Modifier.weight(1f),
                fontSize = 20.sp,
                height = 55.dp
            )
            CalculatorButton(
                text = "=",
                onClick = { viewModel.onEqualsClicked() },
                modifier = Modifier
                    .weight(2f)
                    .height(55.dp),
                isEquals = true,
                fontSize = 20.sp,
                height = 55.dp
            )
        }

        // 9行目: 0, .
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CalculatorButton(
                text = "0",
                onClick = { viewModel.onNumberClicked("0") },
                modifier = Modifier.weight(2f),
                fontSize = 20.sp,
                height = 55.dp
            )
            CalculatorButton(
                text = ".",
                onClick = { viewModel.onDecimalClicked() },
                modifier = Modifier.weight(1f),
                fontSize = 20.sp,
                height = 55.dp
            )
            Spacer(modifier = Modifier.weight(2f)) // =ボタンのスペースを空ける
        }
    }
}
