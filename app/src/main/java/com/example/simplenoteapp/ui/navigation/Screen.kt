package com.example.simplenoteapp.ui.navigation

package com.example.simplenoteapp.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object LoginScreen : Screen("login", "Login")
    object NoteListScreen : Screen("note_list", "Notes")
    object NoteDetailScreen : Screen("note_detail/{noteId}", "Note Detail") {
        fun createRoute(noteId: String) = "note_detail/$noteId"
    }
    object ThemeSettingsScreen : Screen("theme_settings", "Theme Settings")
    object AccountScreen : Screen("login", "Account") // Changed route to "login"
}
