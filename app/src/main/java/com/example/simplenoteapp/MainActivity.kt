package com.example.simplenoteapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.simplenoteapp.ui.components.AppDrawer
import com.example.simplenoteapp.ui.navigation.Screen
import com.example.simplenoteapp.ui.theme.SimpleNoteAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SimpleNoteAppTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        AppDrawer(
                            currentRoute = currentRoute ?: Screen.LoginScreen.route,
                            navigateTo = { screen ->
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                                scope.launch {
                                    drawerState.close()
                                }
                            },
                            closeDrawer = { scope.launch { drawerState.close() } }
                        )
                    }
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                    ) { paddingValues -> // contentPadding parameter is required for Scaffold
                        // Pass paddingValues to NavGraph if it needs to handle insets, otherwise it can be ignored
                        NavGraph(app = application as NoteApplication, navController = navController, drawerState = drawerState)
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeSettingsScreen() {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Text("Theme Settings Screen - Placeholder")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SimpleNoteAppTheme {
        // This preview won't show the drawer, but it's a good practice to have a preview.
        // To preview the drawer, you might need a separate preview function for AppDrawer.
        Text("Main Activity Preview (Scaffold context)")
    }
}
