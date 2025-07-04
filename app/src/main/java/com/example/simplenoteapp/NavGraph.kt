package com.example.simplenoteapp

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.collectAsState
import com.example.simplenoteapp.ui.auth.AuthScreen
import com.example.simplenoteapp.ui.notedetail.NoteDetailScreen
import com.example.simplenoteapp.ui.notelist.NoteListScreen
import com.example.simplenoteapp.viewmodel.AuthViewModel
import com.example.simplenoteapp.viewmodel.NoteViewModel
import com.example.simplenoteapp.viewmodel.NoteViewModelFactory

@Composable
fun NavGraph(app: NoteApplication) {
    val navController = rememberNavController()
    val noteViewModel: NoteViewModel = viewModel(
        factory = NoteViewModelFactory(app.firebaseNoteService)
    )
    val authViewModel: AuthViewModel = viewModel()

    NavHost(navController = navController, startDestination = "auth") {
        composable("auth") {
            AuthScreen(authViewModel = authViewModel) {
                navController.navigate("note_list") {
                    popUpTo("auth") { inclusive = true }
                }
            }
        }
        composable("note_list") {
            NoteListScreen(
                onNoteClick = { noteId ->
                    navController.navigate("note_detail/$noteId")
                },
                onAddNoteClick = {
                    navController.navigate("note_detail/-1")
                },
                viewModel = noteViewModel
            )
        }
        composable(
            "note_detail/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            NoteDetailScreen(
                noteId = if (noteId == "-1") null else noteId,
                onNavigateUp = { navController.navigateUp() },
                viewModel = noteViewModel
            )
        }
    }
}
