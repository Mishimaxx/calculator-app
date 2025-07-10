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