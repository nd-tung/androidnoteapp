package com.example.simplenoteapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.simplenoteapp.ui.auth.LoginScreen
import com.example.simplenoteapp.ui.theme.SimpleNoteAppTheme
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_displaysAllElements() {
        composeTestRule.setContent {
            SimpleNoteAppTheme {
                LoginScreen(onLoginSuccess = {})
            }
        }

        composeTestRule.onNodeWithText("Welcome").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign in or create an account").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign Up").assertIsDisplayed()
    }

    @Test
    fun loginScreen_loginButton_callsOnLoginSuccess() {
        val mockOnLoginSuccess = mock<() -> Unit>()
        composeTestRule.setContent {
            SimpleNoteAppTheme {
                LoginScreen(onLoginSuccess = mockOnLoginSuccess)
            }
        }

        composeTestRule.onNodeWithText("Login").performClick()
        verify(mockOnLoginSuccess).invoke()
    }

    @Test
    fun loginScreen_signUpButton_callsOnLoginSuccess() {
        // For now, Sign Up also calls onLoginSuccess as per current implementation
        val mockOnLoginSuccess = mock<() -> Unit>()
        composeTestRule.setContent {
            SimpleNoteAppTheme {
                LoginScreen(onLoginSuccess = mockOnLoginSuccess)
            }
        }

        composeTestRule.onNodeWithText("Sign Up").performClick()
        verify(mockOnLoginSuccess).invoke()
    }
}
