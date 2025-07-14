package com.example.test2.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.ui.viewmodel.CalculatorViewModel

@Composable
fun ScientificKeypad(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    var isHypMode by remember { mutableStateOf(false) }
    
    // 黒ベースのカラーパレット
    val mediumGray = Color(0xFF3A3A3C)
    val darkGray = mediumGray // 数字ボタンと同じ色に統一
    val lightGray = Color(0xFF505052)
    val redAccent = Color(0xFFFF6B6B) // 演算子用の赤色
    val whiteText = Color.White
    val yellowAccent = Color(0xFFFFD700) // SHIFT用の黄色
    val blueAccent = Color(0xFF6B9DFF) // ALPHA用の青色
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(2.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        
        // 関数ボタン部分（全体で一つのスクロール領域、複数行）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // 1行目: SOLVE、CALC、基本関数
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    
                    CalculatorButton(
                        text = "∫",
                        onClick = { viewModel.onScientificFunctionClicked("integral") },
                        modifier = Modifier.width(65.dp),
                        backgroundColor = darkGray,
                        textColor = redAccent,
                        fontSize = 24.sp,
                        height = 60.dp
                    )
                    
                    CalculatorButton(
                        text = "x!",
                        onClick = { viewModel.onScientificFunctionClicked("!") },
                        modifier = Modifier.width(65.dp),
                        backgroundColor = darkGray,
                        textColor = redAccent,
                        fontSize = 20.sp,
                        height = 60.dp
                    )
                    
                    CalculatorButton(
                        text = "x⁻¹",
                        onClick = { viewModel.onScientificFunctionClicked("1/x") },
                        modifier = Modifier.width(75.dp),
                        backgroundColor = darkGray,
                        textColor = redAccent,
                        fontSize = 18.sp,
                        height = 60.dp
                    )
                    
                    CalculatorButton(
                        text = "√x",
                        onClick = { viewModel.onScientificFunctionClicked("sqrt") },
                        modifier = Modifier.width(65.dp),
                        backgroundColor = darkGray,
                        textColor = redAccent,
                        fontSize = 20.sp,
                        height = 60.dp
                    )
                    
                    CalculatorButton(
                        text = "x²",
                        onClick = { viewModel.onScientificFunctionClicked("^2") },
                        modifier = Modifier.width(65.dp),
                        backgroundColor = darkGray,
                        textColor = redAccent,
                        fontSize = 20.sp,
                        height = 60.dp
                    )
                    
                    CalculatorButton(
                        text = "x³",
                        onClick = { viewModel.onScientificFunctionClicked("^3") },
                        modifier = Modifier.width(65.dp),
                        backgroundColor = darkGray,
                        textColor = redAccent,
                        fontSize = 20.sp,
                        height = 60.dp
                    )
                    
                    CalculatorButton(
                        text = "x^y",
                        onClick = { viewModel.onScientificFunctionClicked("^") },
                        modifier = Modifier.width(75.dp),
                        backgroundColor = darkGray,
                        textColor = redAccent,
                        fontSize = 18.sp,
                        height = 60.dp
                    )
                }
                
                // 2行目: 対数・指数関数
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    CalculatorButton(
                        text = "log",
                        onClick = { viewModel.onScientificFunctionClicked("log") },
                        modifier = Modifier.width(65.dp),
                        backgroundColor = darkGray,
                        textColor = redAccent,
                        fontSize = 18.sp,
                        height = 60.dp
                    )
                    
                    CalculatorButton(
                        text = "ln",
                        onClick = { viewModel.onScientificFunctionClicked("ln") },
                        modifier = Modifier.width(60.dp),
                        backgroundColor = darkGray,
                        textColor = redAccent,
                        fontSize = 18.sp,
                        height = 60.dp
                    )
                    
                    CalculatorButton(
                        text = "10ˣ",
                        onClick = { viewModel.onScientificFunctionClicked("10^x") },
                        modifier = Modifier.width(75.dp),
                        backgroundColor = darkGray,
                        textColor = redAccent,
                        fontSize = 16.sp,
                        height = 60.dp
                    )
                    
                    CalculatorButton(
                        text = "eˣ",
                        onClick = { viewModel.onScientificFunctionClicked("e^x") },
                        modifier = Modifier.width(65.dp),
                        backgroundColor = darkGray,
                        textColor = redAccent,
                        fontSize = 17.sp,
                        height = 60.dp
                    )
                    
                    CalculatorButton(
                        text = "x⁻³",
                        onClick = { viewModel.onScientificFunctionClicked("^-3") },
                        modifier = Modifier.width(70.dp),
                        backgroundColor = darkGray,
                        textColor = redAccent,
                        fontSize = 16.sp,
                        height = 60.dp
                    )
                    
                    CalculatorButton(
                        text = "π",
                        onClick = { viewModel.onNumberClicked("3.141592654") },
                        modifier = Modifier.width(55.dp),
                        backgroundColor = darkGray,
                        textColor = redAccent,
                        fontSize = 24.sp,
                        height = 60.dp
                    )
                    
                    CalculatorButton(
                        text = "e",
                        onClick = { viewModel.onNumberClicked("2.718281828") },
                        modifier = Modifier.width(55.dp),
                        backgroundColor = darkGray,
                        textColor = redAccent,
                        fontSize = 24.sp,
                        height = 60.dp
                    )
                    
                    CalculatorButton(
                        text = "Abs",
                        onClick = { viewModel.onScientificFunctionClicked("abs") },
                        modifier = Modifier.width(65.dp),
                        backgroundColor = darkGray,
                        textColor = redAccent,
                        fontSize = 17.sp,
                        height = 60.dp
                    )
                }
                
                // 3行目: 三角関数
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    CalculatorButton(
                        text = "sin",
                        onClick = { 
                            if (isHypMode) {
                                viewModel.onScientificFunctionClicked("sinh")
                            } else {
                                viewModel.onScientificFunctionClicked("sin")
                            }
                            isHypMode = false
                        },
                        modifier = Modifier.width(65.dp),
                        backgroundColor = darkGray,
                        textColor = redAccent,
                        fontSize = 18.sp,
                        height = 60.dp
                    )
                    
                    CalculatorButton(
                        text = "cos",
                        onClick = { 
                            if (isHypMode) {
                                viewModel.onScientificFunctionClicked("cosh")
                            } else {
                                viewModel.onScientificFunctionClicked("cos")
                            }
                            isHypMode = false
                        },
                        modifier = Modifier.width(65.dp),
                        backgroundColor = darkGray,
                        textColor = redAccent,
                        fontSize = 18.sp,
                        height = 60.dp
                    )
                    
                    CalculatorButton(
                        text = "tan",
                        onClick = { viewModel.onScientificFunctionClicked("tan") },
                        modifier = Modifier.width(65.dp),
                        backgroundColor = darkGray,
                        textColor = redAccent,
                        fontSize = 18.sp,
                        height = 60.dp
                    )
                    
                    CalculatorButton(
                        text = "asin",
                        onClick = { viewModel.onScientificFunctionClicked("asin") },
                        modifier = Modifier.width(70.dp),
                        backgroundColor = darkGray,
                        textColor = redAccent,
                        fontSize = 16.sp,
                        height = 60.dp
                    )
                    
                    CalculatorButton(
                        text = "acos",
                        onClick = { viewModel.onScientificFunctionClicked("acos") },
                        modifier = Modifier.width(70.dp),
                        backgroundColor = darkGray,
                        textColor = redAccent,
                        fontSize = 16.sp,
                        height = 60.dp
                    )
                    
                    CalculatorButton(
                        text = "atan",
                        onClick = { viewModel.onScientificFunctionClicked("atan") },
                        modifier = Modifier.width(70.dp),
                        backgroundColor = darkGray,
                        textColor = redAccent,
                        fontSize = 16.sp,
                        height = 60.dp
                    )
                    
                    CalculatorButton(
                        text = "hyp",
                        onClick = { isHypMode = !isHypMode },
                        modifier = Modifier.width(65.dp),
                        backgroundColor = if (isHypMode) yellowAccent else darkGray,
                        textColor = if (isHypMode) Color.Black else redAccent,
                        fontSize = 17.sp,
                        height = 60.dp
                    )
                }
                
                // 4行目: 双曲線関数・メモリ・その他
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    CalculatorButton(
                        text = "sinh",
                        onClick = { viewModel.onScientificFunctionClicked("sinh") },
                        modifier = Modifier.width(70.dp),
                        backgroundColor = darkGray,
                        textColor = redAccent,
                        fontSize = 16.sp,
                        height = 60.dp
                    )
                    
                    CalculatorButton(
                        text = "cosh",
                        onClick = { viewModel.onScientificFunctionClicked("cosh") },
                        modifier = Modifier.width(70.dp),
                        backgroundColor = darkGray,
                        textColor = redAccent,
                        fontSize = 16.sp,
                        height = 60.dp
                    )
                    
                    CalculatorButton(
                        text = "tanh",
                        onClick = { viewModel.onScientificFunctionClicked("tanh") },
                        modifier = Modifier.width(70.dp),
                        backgroundColor = darkGray,
                        textColor = redAccent,
                        fontSize = 16.sp,
                        height = 60.dp
                    )
                    
                    CalculatorButton(
                        text = "nPr",
                        onClick = { viewModel.onOperatorClicked("nPr") },
                        modifier = Modifier.width(65.dp),
                        backgroundColor = darkGray,
                        textColor = redAccent,
                        fontSize = 17.sp,
                        height = 60.dp
                    )
                    
                    CalculatorButton(
                        text = "nCr",
                        onClick = { viewModel.onOperatorClicked("nCr") },
                        modifier = Modifier.width(65.dp),
                        backgroundColor = darkGray,
                        textColor = redAccent,
                        fontSize = 17.sp,
                        height = 60.dp
                    )
                    
                    CalculatorButton(
                        text = "Rnd",
                        onClick = { viewModel.onScientificFunctionClicked("random") },
                        modifier = Modifier.width(65.dp),
                        backgroundColor = darkGray,
                        textColor = redAccent,
                        fontSize = 17.sp,
                        height = 60.dp
                    )
                    
                    
                    CalculatorButton(
                        text = "M+",
                        onClick = { viewModel.onScientificFunctionClicked("M+") },
                        modifier = Modifier.width(60.dp),
                        backgroundColor = darkGray,
                        textColor = redAccent,
                        fontSize = 17.sp,
                        height = 60.dp
                    )
                    
                    CalculatorButton(
                        text = "M-",
                        onClick = { viewModel.onScientificFunctionClicked("M-") },
                        modifier = Modifier.width(60.dp),
                        backgroundColor = darkGray,
                        textColor = redAccent,
                        fontSize = 17.sp,
                        height = 60.dp
                    )
                    
                }
            }
        }
        
        // 括弧とメモリ関連ボタン
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CalculatorButton(
                text = "(",
                onClick = { viewModel.onParenthesisClicked("(") },
                modifier = Modifier.weight(1f),
                backgroundColor = darkGray,
                textColor = redAccent,
                fontSize = 24.sp,
                height = 60.dp
            )
            
            CalculatorButton(
                text = ")",
                onClick = { viewModel.onParenthesisClicked(")") },
                modifier = Modifier.weight(1f),
                backgroundColor = darkGray,
                textColor = redAccent,
                fontSize = 24.sp,
                height = 60.dp
            )
            
            CalculatorButton(
                text = "×10ˣ",
                onClick = { viewModel.onScientificFunctionClicked("EXP") },
                modifier = Modifier.weight(1f),
                backgroundColor = darkGray,
                textColor = redAccent,
                fontSize = 14.sp,
                height = 60.dp
            )
            
            CalculatorButton(
                text = "Ans",
                onClick = { viewModel.onScientificFunctionClicked("ANS") },
                modifier = Modifier.weight(1f),
                backgroundColor = darkGray,
                textColor = redAccent,
                fontSize = 16.sp,
                height = 60.dp
            )
            
            CalculatorButton(
                text = "±",
                onClick = { viewModel.onScientificFunctionClicked("+/-") },
                modifier = Modifier.weight(1f),
                backgroundColor = darkGray,
                textColor = redAccent,
                fontSize = 20.sp,
                height = 60.dp
            )
        }
        
        // 数字7-9、DEL、AC行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CalculatorButton(
                text = "7",
                onClick = { viewModel.onNumberClicked("7") },
                modifier = Modifier.weight(1f),
                backgroundColor = mediumGray,
                textColor = whiteText,
                fontSize = 24.sp,
                height = 65.dp
            )
            
            CalculatorButton(
                text = "8",
                onClick = { viewModel.onNumberClicked("8") },
                modifier = Modifier.weight(1f),
                backgroundColor = mediumGray,
                textColor = whiteText,
                fontSize = 24.sp,
                height = 65.dp
            )
            
            CalculatorButton(
                text = "9",
                onClick = { viewModel.onNumberClicked("9") },
                modifier = Modifier.weight(1f),
                backgroundColor = mediumGray,
                textColor = whiteText,
                fontSize = 24.sp,
                height = 65.dp
            )
            
            CalculatorButton(
                text = "DEL",
                onClick = { viewModel.onBackspaceClicked() },
                modifier = Modifier.weight(1f),
                backgroundColor = mediumGray,
                textColor = redAccent,
                fontSize = 16.sp,
                height = 65.dp
            )
            
            CalculatorButton(
                text = "AC",
                onClick = { viewModel.onClearClicked() },
                modifier = Modifier.weight(1f),
                backgroundColor = mediumGray,
                textColor = redAccent,
                fontSize = 16.sp,
                height = 65.dp
            )
        }
        
        // 数字4-6、×、÷行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CalculatorButton(
                text = "4",
                onClick = { viewModel.onNumberClicked("4") },
                modifier = Modifier.weight(1f),
                backgroundColor = mediumGray,
                textColor = whiteText,
                fontSize = 24.sp,
                height = 65.dp
            )
            
            CalculatorButton(
                text = "5",
                onClick = { viewModel.onNumberClicked("5") },
                modifier = Modifier.weight(1f),
                backgroundColor = mediumGray,
                textColor = whiteText,
                fontSize = 24.sp,
                height = 65.dp
            )
            
            CalculatorButton(
                text = "6",
                onClick = { viewModel.onNumberClicked("6") },
                modifier = Modifier.weight(1f),
                backgroundColor = mediumGray,
                textColor = whiteText,
                fontSize = 24.sp,
                height = 65.dp
            )
            
            CalculatorButton(
                text = "×",
                onClick = { viewModel.onOperatorClicked("*") },
                modifier = Modifier.weight(1f),
                backgroundColor = darkGray,
                textColor = redAccent,
                fontSize = 24.sp,
                height = 65.dp
            )
            
            CalculatorButton(
                text = "÷",
                onClick = { viewModel.onOperatorClicked("/") },
                modifier = Modifier.weight(1f),
                backgroundColor = darkGray,
                textColor = redAccent,
                fontSize = 24.sp,
                height = 65.dp
            )
        }
        
        // 数字1-3、+、-行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CalculatorButton(
                text = "1",
                onClick = { viewModel.onNumberClicked("1") },
                modifier = Modifier.weight(1f),
                backgroundColor = mediumGray,
                textColor = whiteText,
                fontSize = 24.sp,
                height = 65.dp
            )
            
            CalculatorButton(
                text = "2",
                onClick = { viewModel.onNumberClicked("2") },
                modifier = Modifier.weight(1f),
                backgroundColor = mediumGray,
                textColor = whiteText,
                fontSize = 24.sp,
                height = 65.dp
            )
            
            CalculatorButton(
                text = "3",
                onClick = { viewModel.onNumberClicked("3") },
                modifier = Modifier.weight(1f),
                backgroundColor = mediumGray,
                textColor = whiteText,
                fontSize = 24.sp,
                height = 65.dp
            )
            
            CalculatorButton(
                text = "+",
                onClick = { viewModel.onOperatorClicked("+") },
                modifier = Modifier.weight(1f),
                backgroundColor = darkGray,
                textColor = redAccent,
                fontSize = 24.sp,
                height = 65.dp
            )
            
            CalculatorButton(
                text = "-",
                onClick = { viewModel.onOperatorClicked("-") },
                modifier = Modifier.weight(1f),
                backgroundColor = darkGray,
                textColor = redAccent,
                fontSize = 24.sp,
                height = 65.dp
            )
        }
        
        // 0、小数点、=行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CalculatorButton(
                text = "0",
                onClick = { viewModel.onNumberClicked("0") },
                modifier = Modifier.weight(2f),
                backgroundColor = mediumGray,
                textColor = whiteText,
                fontSize = 24.sp,
                height = 65.dp
            )
            
            CalculatorButton(
                text = ".",
                onClick = { viewModel.onDecimalClicked() },
                modifier = Modifier.weight(1f),
                backgroundColor = mediumGray,
                textColor = whiteText,
                fontSize = 24.sp,
                height = 65.dp
            )
            
            CalculatorButton(
                text = "=",
                onClick = { viewModel.onEqualsClicked() },
                modifier = Modifier.weight(2f),
                backgroundColor = Color(0xFFFF6B6B),
                textColor = whiteText,
                fontSize = 24.sp,
                height = 65.dp
            )
        }
    }
}
