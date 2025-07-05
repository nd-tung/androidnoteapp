package com.example.simplenoteapp.ui.notelist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
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

    // Effect to load notes when the screen is first composed or viewModel changes
    LaunchedEffect(key1 = viewModel) {
        viewModel.loadNotes()
    }

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
                            items(state.data, key = { note -> note.id ?: UUID.randomUUID() }) { note -> // Use note.id as key
                                NoteListItem(note = note, onNoteClick = onNoteClick)
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
fun NoteListItem(note: Note, onNoteClick: (noteId: Long) -> Unit) { // Changed to Long
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(enabled = note.id != null) { // Only clickable if ID exists
                note.id?.let { onNoteClick(it) }
            }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = note.title, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                // Display updated_at if available and parseable, otherwise fallback to local timestamp
                val displayTimestamp = remember(note.updated_at, note.timestamp) {
                    note.updated_at?.let { isoString ->
                        try {
                            // Attempt to parse ISO 8601 string (common from Supabase)
                            // This parsing might need a more robust solution if formats vary
                            val instant = java.time.Instant.parse(isoString)
                            Date.from(instant).time
                        } catch (e: Exception) {
                            note.timestamp // Fallback to local timestamp if parsing fails
                        }
                    } ?: note.timestamp
                }
                Text(
                    text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(displayTimestamp)),
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
