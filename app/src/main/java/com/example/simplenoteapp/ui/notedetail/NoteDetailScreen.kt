package com.example.simplenoteapp.ui.notedetail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.simplenoteapp.data.Note
import com.example.simplenoteapp.viewmodel.NoteViewModel
import com.example.simplenoteapp.viewmodel.NoteUiState

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteIdString: String?, // Renamed from noteId, kept as String from NavGraph
    onNavigateUp: () -> Unit,
    viewModel: NoteViewModel
) {
    val selectedNoteState by viewModel.selectedNoteState.collectAsState()
    val actionResultState by viewModel.actionResult.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var currentNoteId by remember { mutableStateOf<Long?>(null) }
    var isNewNote by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isChecklist by remember { mutableStateOf(false) }
    var checklistItems by remember { mutableStateOf(listOf<String>()) } // Initialized as empty
    var showSyncMenu by remember { mutableStateOf(false) }

    // Parse noteIdString and load note
    LaunchedEffect(key1 = noteIdString) {
        if (noteIdString == "-1" || noteIdString == null) {
            // New note case
            isNewNote = true
            currentNoteId = null
            viewModel.clearSelectedNote() // Clear for new note
            // Reset local fields for new note scenario explicitly
            title = ""
            content = ""
            isChecklist = false
            checklistItems = listOf("") // Start with one empty item for new checklist
        } else {
            // Existing note case
            isNewNote = false
            noteIdString.toLongOrNull()?.let { id ->
                currentNoteId = id
                viewModel.getNoteById(id)
            }
        }
    }

    // Update local UI state when selectedNoteState changes (e.g., after loading)
    LaunchedEffect(selectedNoteState) {
        if (selectedNoteState is NoteUiState.Success && !isNewNote) {
            (selectedNoteState as NoteUiState.Success<Note?>).data?.let { note ->
                title = note.title
                content = note.content
                isChecklist = note.isChecklist
                if (isChecklist) {
                    // Basic split, ensure content is not empty, provide default if it is
                    checklistItems = if (note.content.isNotEmpty()) note.content.split("\n") else listOf("")
                } else {
                    checklistItems = listOf() // Clear if not a checklist
                }
            }
        }
    }

    // Handle action results (save, delete) - only for showing errors, navigation is immediate
    LaunchedEffect(actionResultState) {
        when (val result = actionResultState) {
            is NoteUiState.Error -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = result.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
            else -> { /* No need to handle success or loading here since navigation happens immediately */ }
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (isNewNote) "New Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isNewNote && currentNoteId != null) {
                        // Sync dropdown menu
                        Box {
                            IconButton(onClick = { showSyncMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Sync Options")
                            }
                            DropdownMenu(
                                expanded = showSyncMenu,
                                onDismissRequest = { showSyncMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Sync to Cloud") },
                                    onClick = {
                                        showSyncMenu = false
                                        (selectedNoteState as? NoteUiState.Success<Note?>)?.data?.let { note ->
                                            viewModel.syncNoteToCloud(note)
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Send, contentDescription = null)
                                    }
                                )
                                val currentNote = (selectedNoteState as? NoteUiState.Success<Note?>)?.data
                                if (currentNote?.serverId != null) {
                                    DropdownMenuItem(
                                        text = { Text("Sync from Cloud") },
                                        onClick = {
                                            showSyncMenu = false
                                            currentNote.serverId?.let { serverId ->
                                                viewModel.syncNoteFromCloud(serverId)
                                            }
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Info, contentDescription = null)
                                        }
                                    )
                                }
                            }
                        }
                        
                        IconButton(onClick = {
                            (selectedNoteState as? NoteUiState.Success<Note?>)?.data?.let { noteToDelete ->
                                viewModel.deleteNote(noteToDelete)
                                onNavigateUp() // Navigate after initiating delete
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Note")
                        }
                    }
                    IconButton(onClick = {
                        val finalContent = if (isChecklist) {
                            checklistItems.joinToString("\n")
                        } else {
                            content
                        }
                        val noteToSave = if (!isNewNote && currentNoteId != null) {
                            // Editing existing note - mark as needing sync
                            val currentNote = (selectedNoteState as? NoteUiState.Success<Note?>)?.data
                            Note(
                                id = currentNoteId!!,
                                title = title.trim(),
                                content = finalContent.trim(),
                                isChecklist = isChecklist,
                                serverId = currentNote?.serverId,
                                isSynced = false,
                                needsSync = true,
                                lastSyncTime = currentNote?.lastSyncTime ?: 0L
                            )
                        } else {
                            // New note - will be marked as needing sync automatically
                            Note(
                                title = title.trim(),
                                content = finalContent.trim(),
                                isChecklist = isChecklist,
                                needsSync = true
                            )
                        }
                        if (isNewNote) {
                            viewModel.addNote(noteToSave)
                        } else {
                            viewModel.updateNote(noteToSave)
                        }
                        onNavigateUp() // Navigate after initiating save
                    }) {
                        Icon(Icons.Default.Done, contentDescription = "Save Note")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = selectedNoteState) {
            is NoteUiState.Loading -> {
                if (!isNewNote && currentNoteId != null) { // Only show loading if we are expecting a note
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else { // New note, show editor immediately
                    NoteEditorContent(
                        modifier = Modifier.padding(paddingValues),
                        title = title,
                        onTitleChange = { title = it },
                        content = content,
                        onContentChange = { content = it },
                        isChecklist = isChecklist,
                        onIsChecklistChange = { isChecklist = it },
                        checklistItems = checklistItems,
                        onChecklistItemsChange = { checklistItems = it },
                        note = null // New note
                    )
                }
            }
            is NoteUiState.Success -> {
                // Data is already set to local state vars via LaunchedEffect(selectedNoteState)
                val currentNote = (state as NoteUiState.Success<Note?>).data
                NoteEditorContent(
                    modifier = Modifier.padding(paddingValues),
                    title = title,
                    onTitleChange = { title = it },
                    content = content,
                    onContentChange = { content = it },
                    isChecklist = isChecklist,
                    onIsChecklistChange = { isChecklist = it },
                    checklistItems = checklistItems,
                    onChecklistItemsChange = { checklistItems = it },
                    note = currentNote
                )
            }
            is NoteUiState.Error -> {
                 Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun NoteEditorContent(
    modifier: Modifier = Modifier,
    title: String,
    onTitleChange: (String) -> Unit,
    content: String,
    onContentChange: (String) -> Unit,
    isChecklist: Boolean,
    onIsChecklistChange: (Boolean) -> Unit,
    checklistItems: List<String>,
    onChecklistItemsChange: (List<String>) -> Unit,
    note: Note? = null // Current note for sync status
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        // Sync status indicator
        note?.let { currentNote ->
            SyncStatusCard(note = currentNote)
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Is Checklist?")
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isChecklist,
                onCheckedChange = {
                    onIsChecklistChange(it)
                    if (it && checklistItems.isEmpty()) { // If switching to checklist and it's empty
                        onChecklistItemsChange(listOf("")) // Add one empty item
                    }
                }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (isChecklist) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(checklistItems) { index, item ->
                    ChecklistItemEditor(
                        item = item,
                        onItemChange = { updatedText ->
                            val newList = checklistItems.toMutableList()
                            newList[index] = updatedText
                            onChecklistItemsChange(newList)
                        },
                        onCheckedChange = { isChecked ->
                            val currentText = item.removePrefix("[x] ").removePrefix("[ ] ")
                            val updatedItem = if (isChecked) "[x] $currentText" else "[ ] $currentText"
                            val newList = checklistItems.toMutableList()
                            newList[index] = updatedItem
                            onChecklistItemsChange(newList)
                        }
                    )
                }
                item {
                    Button(
                        onClick = { onChecklistItemsChange(checklistItems + "[ ] ") }, // Add new item with unchecked prefix
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Add Checklist Item")
                    }
                }
            }
        } else {
            OutlinedTextField(
                value = content,
                onValueChange = onContentChange,
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                maxLines = 10 // Allow more lines for content
            )
        }
    }
}

@Composable
fun ChecklistItemEditor(
    item: String,
    onItemChange: (String) -> Unit,
    onCheckedChange: (Boolean) -> Unit
) {
    val isChecked = item.startsWith("[x] ")
    val text = item.removePrefix("[x] ").removePrefix("[ ] ")

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
        OutlinedTextField(
            value = text,
            onValueChange = { newText ->
                onItemChange(if (isChecked) "[x] $newText" else "[ ] $newText")
            },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
    }
}

@Composable
fun SyncStatusCard(note: Note) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                note.needsSync -> MaterialTheme.colorScheme.errorContainer
                note.isSynced -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when {
                    note.needsSync -> Icons.Default.Refresh
                    note.isSynced -> Icons.Default.Check
                    else -> Icons.Default.Close
                },
                contentDescription = null,
                tint = when {
                    note.needsSync -> MaterialTheme.colorScheme.onErrorContainer
                    note.isSynced -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when {
                        note.needsSync -> "Needs Sync"
                        note.isSynced -> "Synced"
                        else -> "Not Synced"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        note.needsSync -> MaterialTheme.colorScheme.onErrorContainer
                        note.isSynced -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                if (note.lastSyncTime > 0) {
                    Text(
                        text = "Last sync: ${java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(note.lastSyncTime))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            note.needsSync -> MaterialTheme.colorScheme.onErrorContainer
                            note.isSynced -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            if (note.serverId != null) {
                Text(
                    text = "ID: ${note.serverId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        note.needsSync -> MaterialTheme.colorScheme.onErrorContainer
                        note.isSynced -> MaterialTheme.colorScheme.onPrimaryContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}
