package com.example.simplenoteapp.ui.notelist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.simplenoteapp.R
import com.example.simplenoteapp.data.Note
import com.example.simplenoteapp.viewmodel.NoteUiState
import com.example.simplenoteapp.viewmodel.NoteViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    onNoteClick: (noteId: Long) -> Unit,
    onAddNoteClick: () -> Unit,
    viewModel: NoteViewModel,
    drawerState: DrawerState
) {
    val notesState by viewModel.notesState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) },
                navigationIcon = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            drawerState.open()
                        }
                    }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshAllNotes() }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh_notes_description))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNoteClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_note_description), tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
        ) {
            when (val state = notesState) {
                is NoteUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is NoteUiState.Success -> {
                    if (state.data.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp), // Consistent padding
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp), // Consistent icon size
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) // Consistent color
                            )
                            Spacer(modifier = Modifier.height(24.dp)) // Consistent spacing
                            Text(
                                text = "No notes yet",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold, // Consistent font weight
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f) // Consistent primary text
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap the '+' button to add a new note.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                        ) {
                            items(state.data, key = { note -> note.id }) { note ->
                                NoteListItem(
                                    note = note,
                                    onNoteClick = onNoteClick,
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
                is NoteUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refreshAllNotes() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoteListItem(
    note: Note,
    onNoteClick: (noteId: Long) -> Unit,
    viewModel: NoteViewModel
) {
    var showSyncMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp) // Reduced vertical padding
            .clickable { onNoteClick(note.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // Slight elevation for depth
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // Clean surface color for all notes
        ),
        border = when {
            note.needsSync -> androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
            note.isSynced -> androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            else -> null // No border for regular notes
        },
        shape = MaterialTheme.shapes.medium // Consistent card shape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp), // Consistent padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = note.title.ifEmpty { "Untitled Note" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold, // Consistent font weight
                        color = MaterialTheme.colorScheme.onSurface, // Consistent primary text color
                        modifier = Modifier.weight(1f).padding(end = 12.dp) // Consistent spacing
                    )
                    // Single sync status indicator
                    Icon(
                        imageVector = when {
                            note.needsSync -> Icons.Default.Refresh
                            note.isSynced -> Icons.Default.Check
                            else -> Icons.Default.Close
                        },
                        contentDescription = when {
                            note.needsSync -> "Needs Sync"
                            note.isSynced -> "Synced to cloud"
                            else -> "Not Synced"
                        },
                        tint = when {
                            note.needsSync -> MaterialTheme.colorScheme.error.copy(alpha = 0.8f) // Consistent error color
                            note.isSynced -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) // Consistent success color
                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        },
                        modifier = Modifier.size(18.dp) // Consistent icon size
                    )
                }
                Spacer(modifier = Modifier.height(6.dp)) // Consistent spacing
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date(note.timestamp)), // Improved format
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) // Consistent secondary text color
                )
                if (note.content.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp)) // Consistent spacing
                    Text(
                        text = note.content.take(100) + if (note.content.length > 100) "..." else "", // Shorter preview for consistency
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2, // Consistent preview lines
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), // Consistent content color
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                    )
                }
            }

            // Icons row for checklist and menu
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp) // Consistent icon spacing
            ) {
                if (note.isChecklist) {
                    Icon(
                        Icons.Default.List,
                        contentDescription = "Checklist note",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), // Consistent color scheme
                        modifier = Modifier.size(18.dp) // Consistent icon size
                    )
                }
                
                // Quick sync menu - more subtle
                Box {
                    IconButton(
                        onClick = { showSyncMenu = true },
                        modifier = Modifier.size(40.dp) // Consistent touch target
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Note options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showSyncMenu,
                    onDismissRequest = { showSyncMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Sync to Cloud") },
                        onClick = {
                            showSyncMenu = false
                            viewModel.syncNoteToCloud(note)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Send, contentDescription = null)
                        }
                    )
                    if (note.serverId != null) {
                        DropdownMenuItem(
                            text = { Text("Sync from Cloud") },
                            onClick = {
                                showSyncMenu = false
                                viewModel.syncNoteFromCloud(note.serverId)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Info, contentDescription = null)
                            }
                        )
                    }
                    // Consider adding Delete option here later if needed
                }
            }
        }
    }
}

// Placeholder string resources (should be in strings.xml)
// R.string.open_drawer_description = "Open navigation drawer"
// R.string.refresh_notes_description = "Refresh notes"
// R.string.add_note_description = "Add new note"
