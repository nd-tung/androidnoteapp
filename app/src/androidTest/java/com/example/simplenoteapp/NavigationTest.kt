package com.example.simplenoteapp

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.simplenoteapp.ui.auth.LoginScreen
import com.example.simplenoteapp.ui.navigation.Screen
import com.example.simplenoteapp.ui.notelist.NoteListScreen
import com.example.simplenoteapp.ui.theme.SimpleNoteAppTheme
import kotlinx.coroutines.launch
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

@ExperimentalMaterial3Api
class NavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var navController: NavHostController
    private lateinit var app: NoteApplication

    @Before
    fun setUp() {
        app = composeTestRule.activity.application as NoteApplication
        composeTestRule.setContent {
            navController = rememberNavController()
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            SimpleNoteAppTheme {
                MainScreenWithDrawer(
                    navController = navController,
                    app = app,
                    drawerState = drawerState,
                    scope = scope
                )
            }
        }
    }

    @Test
    fun appNavigation_loginToNoteList_thenOpenDrawerAndNavigate() {
        // Start on LoginScreen
        composeTestRule.onNodeWithText("Login/Sign Up").assertIsDisplayed() // From LoginScreen title

        // Perform login (simulated)
        composeTestRule.onNodeWithText("Login").performClick()

        // Should navigate to NoteListScreen
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.app_name)).assertIsDisplayed() // TopAppBar title on NoteListScreen
        composeTestRule.onNodeWithText("No notes yet. Tap '+' to add one!").assertIsDisplayed() // Assuming no notes initially

        // Open Drawer
        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.open_drawer_description)).performClick()

        // Verify drawer items are visible
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.notes_screen_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.account_screen_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.theme_settings_title)).assertIsDisplayed()

        // Navigate to Account (which is LoginScreen again for now)
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.account_screen_title)).performClick()
        composeTestRule.onNodeWithText("Login/Sign Up").assertIsDisplayed() // Back to LoginScreen

        // Open Drawer again (from LoginScreen, assuming drawer is accessible or test setup implies it)
        // For this test, we'll assume the drawer can be opened from LoginScreen if it's the "Account" destination.
        // This might need adjustment based on actual drawer availability logic on LoginScreen.
        // Let's navigate to NoteList first to ensure drawer is present.
        composeTestRule.onNodeWithText("Login").performClick() // Go back to NoteList
        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.open_drawer_description)).performClick()


        // Navigate to Theme Settings
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.theme_settings_title)).performClick()
        composeTestRule.onNodeWithText("Theme Settings Screen - Placeholder").assertIsDisplayed() // Check for placeholder text
    }

    // Helper composable to setup the UI for testing, similar to MainActivity's setContent
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreenWithDrawer(
        navController: NavHostController,
        app: NoteApplication,
        drawerState: androidx.compose.material3.DrawerState,
        scope: kotlinx.coroutines.CoroutineScope
    ) {
        com.example.simplenoteapp.ui.components.AppDrawer(
            currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: Screen.LoginScreen.route,
            navigateTo = { screen ->
                navController.navigate(screen.route) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
                scope.launch { drawerState.close() }
            },
            closeDrawer = { scope.launch { drawerState.close() } }
        )

        NavGraph(app = app, navController = navController, drawerState = drawerState)
    }
}
