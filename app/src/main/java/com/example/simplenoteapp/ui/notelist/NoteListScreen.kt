package com.example.simplenoteapp.ui.notelist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.List






import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.simplenoteapp.data.Note
import com.example.simplenoteapp.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    onNoteClick: (String?) -> Unit,
    onAddNoteClick: () -> Unit,
    viewModel: NoteViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    val notes by if (searchQuery.isEmpty()) {
        viewModel.getNotes().collectAsState(initial = emptyList())
    } else {
        viewModel.searchNotes("%$searchQuery%").collectAsState(initial = emptyList())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notes") },
                actions = {
                    var isSearchVisible by remember { mutableStateOf(false) }
                    if (isSearchVisible) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        IconButton(onClick = { isSearchVisible = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNoteClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            items(notes) { note ->
                NoteListItem(note = note, onNoteClick = onNoteClick)
            }
        }
    }
}

@Composable
fun NoteListItem(note: Note, onNoteClick: (String?) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { note.id?.let { onNoteClick(it) } }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = note.title, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(note.timestamp)),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (note.isChecklist) {
                Icon(Icons.Filled.List, contentDescription = "Checklist")
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Checklist", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
