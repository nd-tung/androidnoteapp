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
import androidx.compose.ui.unit.dp
import com.example.simplenoteapp.data.Note
import com.example.simplenoteapp.viewmodel.NoteUiState
import com.example.simplenoteapp.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    onNoteClick: (noteId: Long) -> Unit, // Changed to Long
    onAddNoteClick: () -> Unit,
    viewModel: NoteViewModel
) {
    val notesState by viewModel.notesState.collectAsState()

    // Only refresh on manual refresh, not on every composition
    // The ViewModel's init block already loads notes initially
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notes") },
                actions = {
                    IconButton(onClick = { viewModel.refreshAllNotes() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Notes")
                    }
                    // Search bar removed for now to simplify
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNoteClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (val state = notesState) {
                is NoteUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is NoteUiState.Success -> {
                    if (state.data.isEmpty()) {
                        Text(
                            text = "No notes yet. Tap '+' to add one!",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
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
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onNoteClick(note.id) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = note.title, 
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    // Sync status indicator
                    Icon(
                        imageVector = when {
                            note.needsSync -> Icons.Default.Refresh
                            note.isSynced -> Icons.Default.Check
                            else -> Icons.Default.Close
                        },
                        contentDescription = when {
                            note.needsSync -> "Needs Sync"
                            note.isSynced -> "Synced"
                            else -> "Not Synced"
                        },
                        tint = when {
                            note.needsSync -> MaterialTheme.colorScheme.error
                            note.isSynced -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Display timestamp using local timestamp
                Text(
                    text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(note.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                 if (note.content.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = note.content.take(100) + if (note.content.length > 100) "..." else "", // Show a preview
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2
                    )
                }
            }
            
            // Quick sync menu
            Box {
                IconButton(
                    onClick = { showSyncMenu = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.MoreVert, 
                        contentDescription = "Sync Options",
                        modifier = Modifier.size(20.dp)
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
                }
            }
            
            if (note.isChecklist) {
                Icon(
                    Icons.Filled.List,
                    contentDescription = "Checklist",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
