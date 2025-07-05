package com.example.simplenoteapp

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
// import androidx.compose.runtime.collectAsState // This import seems unused here
import com.example.simplenoteapp.ui.auth.AuthScreen
import com.example.simplenoteapp.ui.notedetail.NoteDetailScreen
import com.example.simplenoteapp.ui.notelist.NoteListScreen
import com.example.simplenoteapp.viewmodel.AuthViewModel
import com.example.simplenoteapp.viewmodel.NoteViewModel
import com.example.simplenoteapp.viewmodel.NoteViewModelFactory // Ensure this points to the factory in the viewmodel package

@Composable
fun NavGraph(app: NoteApplication) {
    val navController = rememberNavController()

    // Correctly provide the NoteRepository from NoteApplication to the NoteViewModelFactory
    val noteViewModel: NoteViewModel = viewModel(
        factory = NoteViewModelFactory(app.noteRepository) // Changed to app.noteRepository
    )
    val authViewModel: AuthViewModel = viewModel() // Assuming AuthViewModel doesn't need a custom factory for now

    NavHost(navController = navController, startDestination = "auth") { // Assuming "auth" is still the start destination
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
                    // NoteListScreen will now likely pass Long for noteId if it comes from Note.id
                    navController.navigate("note_detail/$noteId")
                },
                onAddNoteClick = {
                    navController.navigate("note_detail/-1") // Passing -1 (or any placeholder for new)
                },
                viewModel = noteViewModel
            )
        }
        composable(
            "note_detail/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.StringType }) // Kept as String for Nav Arg, convert in screen
        ) { backStackEntry ->
            val noteIdString = backStackEntry.arguments?.getString("noteId")
            // Conversion from String to Long? will happen in NoteDetailScreen or passed to ViewModel which expects Long
            NoteDetailScreen(
                noteIdString = noteIdString, // Pass the string, let DetailScreen handle conversion
                onNavigateUp = { navController.navigateUp() },
                viewModel = noteViewModel
            )
        }
    }
}
