package com.example.simplenoteapp.ui.notedetail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplenoteapp.data.Note
import com.example.simplenoteapp.viewmodel.NoteViewModel

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: String?,
    onNavigateUp: () -> Unit,
    viewModel: NoteViewModel
) {
    val note by if (noteId != null) {
        viewModel.getNoteById(noteId!!).collectAsState(initial = null)
    } else {
        remember { mutableStateOf(null) }
    }

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isChecklist by remember { mutableStateOf(false) }
    var checklistItems by remember { mutableStateOf(listOf("")) }

    LaunchedEffect(note) {
        note?.let {
            title = it.title
            content = it.content
            isChecklist = it.isChecklist
            if (isChecklist) {
                checklistItems = content.split("\n")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == null) "New Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (noteId != null) {
                        IconButton(onClick = {
                            note?.let {
                                viewModel.deleteNote(it)
                                onNavigateUp()
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                    IconButton(onClick = {
                        val finalContent = if (isChecklist) {
                            checklistItems.joinToString("\n")
                        } else {
                            content
                        }
                        val newNote = note?.copy(
                            title = title,
                            content = finalContent,
                            isChecklist = isChecklist
                        ) ?: Note(
                            title = title,
                            content = finalContent,
                            isChecklist = isChecklist,
                            id = null
                        )
                        if (noteId == null) {
                            viewModel.addNote(newNote)
                        } else {
                            viewModel.updateNote(newNote)
                        }
                        onNavigateUp()
                    }) {
                        Icon(Icons.Default.Done, contentDescription = "Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Checklist")
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = isChecklist,
                    onCheckedChange = { isChecklist = it }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (isChecklist) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(checklistItems) { index, item ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = item.startsWith("[x] "),
                                onCheckedChange = {
                                    val updatedItem = if (it) {
                                        "[x] ${item.removePrefix("[ ] ")}"
                                    } else {
                                        "[ ] ${item.removePrefix("[x] ")}"
                                    }
                                    checklistItems = checklistItems.toMutableList().also {
                                        it[index] = updatedItem
                                    }
                                }
                            )
                            OutlinedTextField(
                                value = item.removePrefix("[x] ").removePrefix("[ ] "),
                                onValueChange = { newText ->
                                    checklistItems = checklistItems.toMutableList().also {
                                        it[index] = if (item.startsWith("[x] ")) "[x] $newText" else "[ ] $newText"
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    item {
                        Button(onClick = { checklistItems = checklistItems + "" }) {
                            Text("Add Item")
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }
}
