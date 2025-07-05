package com.example.simplenoteapp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.simplenoteapp.ui.components.AppDrawer
import com.example.simplenoteapp.ui.navigation.Screen
import com.example.simplenoteapp.ui.theme.SimpleNoteAppTheme
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class AppDrawerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun appDrawer_displaysItemsAndHandlesNavigation() {
        val mockNavigateTo = mock<(Screen) -> Unit>()
        val mockCloseDrawer = mock<() -> Unit>()

        composeTestRule.setContent {
            SimpleNoteAppTheme {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
                val scope = rememberCoroutineScope()
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        AppDrawer(
                            currentRoute = Screen.NoteListScreen.route,
                            navigateTo = mockNavigateTo,
                            closeDrawer = { scope.launch { mockCloseDrawer() } }
                        )
                    }
                ) {
                    // Empty content for testing the drawer itself
                }
            }
        }

        // Verify items are displayed
        composeTestRule.onNodeWithText("Notes").assertIsDisplayed()
        composeTestRule.onNodeWithText("Account").assertIsDisplayed()
        composeTestRule.onNodeWithText("Theme Settings").assertIsDisplayed()

        // Simulate clicking on "Account"
        composeTestRule.onNodeWithText("Account").performClick()
        verify(mockNavigateTo).invoke(Screen.AccountScreen)
        verify(mockCloseDrawer).invoke() // closeDrawer should also be called

        // Simulate clicking on "Theme Settings"
        composeTestRule.onNodeWithText("Theme Settings").performClick()
        verify(mockNavigateTo).invoke(Screen.ThemeSettingsScreen)
        verify(mockCloseDrawer).invoke() // closeDrawer should also be called

        // Simulate clicking on "Notes" (current route, but still should call navigate)
        composeTestRule.onNodeWithText("Notes").performClick()
        verify(mockNavigateTo).invoke(Screen.NoteListScreen)
        verify(mockCloseDrawer).invoke()
    }
}
