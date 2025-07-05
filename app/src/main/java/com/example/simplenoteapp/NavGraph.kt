package com.example.simplenoteapp

import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
// import androidx.compose.runtime.collectAsState // This import seems unused here
import com.example.simplenoteapp.ui.auth.LoginScreen // Changed from AuthScreen
import com.example.simplenoteapp.ui.notedetail.NoteDetailScreen
import com.example.simplenoteapp.ui.notelist.NoteListScreen
import com.example.simplenoteapp.ui.navigation.Screen
import com.example.simplenoteapp.viewmodel.AuthViewModel
import com.example.simplenoteapp.viewmodel.NoteViewModel
import com.example.simplenoteapp.viewmodel.NoteViewModelFactory // Ensure this points to the factory in the viewmodel package

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph(app: NoteApplication, navController: NavHostController, drawerState: DrawerState) {
    // Correctly provide the NoteRepository from NoteApplication to the NoteViewModelFactory
    val noteViewModel: NoteViewModel = viewModel(
        factory = NoteViewModelFactory(app.noteRepository) // Changed to app.noteRepository
    )
    val authViewModel: AuthViewModel = viewModel() // Assuming AuthViewModel doesn't need a custom factory for now

    NavHost(navController = navController, startDestination = Screen.LoginScreen.route) {
        composable(Screen.LoginScreen.route) {
            LoginScreen {
                navController.navigate(Screen.NoteListScreen.route) {
                    popUpTo(Screen.LoginScreen.route) { inclusive = true }
                }
            }
        }
        composable(Screen.NoteListScreen.route) {
            NoteListScreen(
                onNoteClick = { noteId ->
                    navController.navigate(Screen.NoteDetailScreen.createRoute(noteId.toString()))
                },
                onAddNoteClick = {
                    navController.navigate(Screen.NoteDetailScreen.createRoute("-1")) // Passing -1 (or any placeholder for new)
                },
                viewModel = noteViewModel,
                drawerState = drawerState
            )
        }
        composable(
            route = Screen.NoteDetailScreen.route,
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val noteIdString = backStackEntry.arguments?.getString("noteId")
            NoteDetailScreen(
                noteIdString = noteIdString,
                onNavigateUp = { navController.navigateUp() },
                viewModel = noteViewModel,
                drawerState = drawerState
            )
        }
        composable(Screen.ThemeSettingsScreen.route) {
            ThemeSettingsScreen() // Placeholder for Theme Settings
        }
        composable(Screen.AccountScreen.route) { // Added route for AccountScreen
            LoginScreen { // Navigates to LoginScreen for now
                navController.navigate(Screen.NoteListScreen.route) {
                    popUpTo(Screen.LoginScreen.route) { inclusive = true }
                }
            }
        }
    }
}
