package com.example.simplenoteapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.simplenoteapp.R
import com.example.simplenoteapp.ui.navigation.Screen

data class DrawerItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

@Composable
fun AppDrawer(
    currentRoute: String,
    navigateTo: (Screen) -> Unit,
    closeDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val drawerItems = listOf(
        DrawerItem(Screen.NoteListScreen, Icons.Filled.Home, stringResource(id = R.string.notes_screen_title)),
        DrawerItem(Screen.AccountScreen, Icons.Filled.AccountCircle, stringResource(id = R.string.account_screen_title)),
        DrawerItem(Screen.ThemeSettingsScreen, Icons.Filled.Settings, stringResource(id = R.string.theme_settings_title))
    )

    ModalDrawerSheet(modifier) {
        Spacer(Modifier.height(12.dp))
        drawerItems.forEach { item ->
            NavigationDrawerItem(
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(item.label) },
                selected = currentRoute == item.screen.route,
                onClick = {
                    navigateTo(item.screen)
                    closeDrawer()
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}

// Adding string resources that will be needed
// These would typically go into res/values/strings.xml, but for this exercise,
// I'll add them here as comments to indicate what's needed.
// R.string.notes_screen_title = "Notes"
// R.string.account_screen_title = "Account"
// R.string.theme_settings_title = "Theme Settings"
