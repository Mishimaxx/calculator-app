# Calculator App Project

## Project Overview
Android電卓アプリ（Kotlin + Jetpack Compose）

## Key Features
- 基本的な四則演算
- 科学計算機能
- 計算履歴の保存・表示
- ダークモード対応
- ハンバーガーメニュー付きUI

## Architecture
- MVVM pattern with ViewModel
- Room Database for calculation history
- Jetpack Compose for UI
- Repository pattern for data management

## Key Files
- `app/src/main/java/com/example/test2/ui/screens/MainCalculatorScreen.kt` - メイン画面
- `app/src/main/java/com/example/test2/ui/viewmodel/CalculatorViewModel.kt` - ビューモデル
- `app/src/main/java/com/example/test2/calculator/CalculationEngine.kt` - 計算エンジン
- `app/src/main/java/com/example/test2/data/database/CalculatorDatabase.kt` - データベース

## Development Commands
- Build: `./gradlew build`
- Run tests: `./gradlew test`
- Clean: `./gradlew clean`

## Notes
- Package name: com.example.test2
- Target SDK: Android API level as specified in build.gradle
- Uses Room database for persistence
- Supports both basic and scientific calculator modes

## Preview Rules (途中の答えの表示ルール)
- 基本方針: 途中の答えは「=」を付けて評価可能な部分の結果を表示します（例: `33+22` → `=55`）。
- エラーパターン（例: `3÷0`）は非表示。
- 平方根（√）:
	- 入力途中（`√□`）はプレビューも `√□` を表示し評価しない。
	- 式全体が `√<数値>` の場合は評価して `=<結果>` を表示（例: `√9` → `=3`）。
	- 立方根（`³√`）はこの特例の対象外。
- 階乗（!）:
	- 式全体が `<数値>!` の場合は評価して `=<結果>` を表示（例: `5!` → `=120`）。
	- オペランド未確定（例: `!`, `3+!`）はプレビューに `□!` を表示し評価しない。
 - 関数（sin, cos, tan, asin, acos, atan, sinh, cosh, tanh, ln, log）:
	 - 引数未確定（`func(`）はプレビューに `func(□` を表示し評価しない。
	 - `func(<数値>)` の形は評価して `=<結果>` を表示。