package com.example.simplenoteapp.ui.notelist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.simplenoteapp.data.Note
import com.example.simplenoteapp.viewmodel.NoteUiState
import com.example.simplenoteapp.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.*

package com.example.simplenoteapp.ui.notelist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
                        Icon(Icons.Filled.Menu, contentDescription = stringResource(R.string.open_drawer_description))
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
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.NoteAdd, // Changed to a more relevant icon
                                contentDescription = null,
                                modifier = Modifier.size(120.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No notes yet.",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Tap the '+' button to add a new note.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
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
                            imageVector = Icons.Filled.ErrorOutline,
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
            .padding(vertical = 4.dp) // Reduced vertical padding for tighter list
            .clickable { onNoteClick(note.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = note.title.ifEmpty { "Untitled Note" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // Sync status indicator
                    Icon(
                        imageVector = when {
                            note.needsSync -> Icons.Default.SyncProblem // More indicative icon
                            note.isSynced -> Icons.Default.CloudDone // Clearer synced icon
                            else -> Icons.Default.CloudOff // Clearer not synced icon
                        },
                        contentDescription = when {
                            note.needsSync -> "Needs Sync"
                            note.isSynced -> "Note synced to cloud"
                            else -> "Not Synced"
                        },
                        tint = when {
                            note.needsSync -> MaterialTheme.colorScheme.error
                            note.isSynced -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        },
                        modifier = Modifier.size(20.dp) // Slightly larger icon
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(note.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                if (note.content.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = note.content.take(120) + if (note.content.length > 120) "..." else "",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3, // Allow more lines for preview
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (note.isChecklist) {
                Icon(
                    Icons.Filled.Checklist, // Using Checklist icon
                    contentDescription = "Checklist note",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(start = 8.dp) // Add some padding
                )
            }

            // Quick sync menu
            Box {
                IconButton(
                    onClick = { showSyncMenu = true },
                    modifier = Modifier.size(48.dp) // Slightly larger touch target
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Note options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                            Icon(Icons.Default.CloudUpload, contentDescription = "Sync to cloud")
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
                                Icon(Icons.Default.CloudDownload, contentDescription = "Sync from cloud")
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
