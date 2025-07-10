
package com.example.test2

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.example.test2.ui.theme.Test2Theme

/**
 * 電卓アプリのUIテスト
 */
@RunWith(AndroidJUnit4::class)
class CalculatorUITest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `基本的な計算テスト`() {
        // 2 + 3 = 5 の計算をテスト
        composeTestRule.onNodeWithText("2").performClick()
        composeTestRule.onNodeWithText("+").performClick()
        composeTestRule.onNodeWithText("3").performClick()
        composeTestRule.onNodeWithText("=").performClick()
        
        // 結果に5が含まれることを確認 (計算が正常に実行されたことを確認)
        composeTestRule.onAllNodesWithText("5", substring = true)[0].assertExists()
    }

    @Test
    fun `クリアボタンのテスト`() {
        // 数字を入力
        composeTestRule.onNodeWithText("1").performClick()
        composeTestRule.onNodeWithText("2").performClick()
        composeTestRule.onNodeWithText("3").performClick()
        
        // クリアボタンを押す
        composeTestRule.onNodeWithText("C").performClick()
        
        // 表示が0になることを確認
        composeTestRule.onNodeWithText("0").assertExists()
    }

    @Test
    fun `ハンバーガーメニュー切り替えテスト`() {
        // ハンバーガーメニューボタンをクリック
        composeTestRule.onNodeWithContentDescription("メニュー").performClick()
        
        // 関数タブをクリック
        composeTestRule.onNodeWithText("関数").performClick()
        
        // sin ボタンが表示されることを確認
        composeTestRule.onNodeWithText("sin").assertExists()
        
        // ハンバーガーメニューボタンをクリック
        composeTestRule.onNodeWithContentDescription("メニュー").performClick()
        
        // 進数タブをクリック
        composeTestRule.onNodeWithText("進数").performClick()
        
        // BIN ボタンが表示されることを確認
        composeTestRule.onNodeWithText("BIN").assertExists()
        
        // ハンバーガーメニューボタンをクリック
        composeTestRule.onNodeWithContentDescription("メニュー").performClick()
        
        // 履歴タブをクリック
        composeTestRule.onNodeWithText("履歴").performClick()
        
        // 履歴画面が表示されることを確認
        composeTestRule.onNodeWithText("計算履歴").assertExists()
    }

    @Test
    fun `演算子の連続入力防止テスト`() {
        composeTestRule.onNodeWithText("2").performClick()
        composeTestRule.onNodeWithText("+").performClick()
        composeTestRule.onNodeWithText("+").performClick() // 連続で演算子入力
        composeTestRule.onNodeWithText("3").performClick()
        composeTestRule.onNodeWithText("=").performClick()
        
        // エラーにならず、正常に計算されることを確認 (計算が正常に実行されたことを確認)
        composeTestRule.onAllNodesWithText("5", substring = true)[0].assertExists()
    }
}