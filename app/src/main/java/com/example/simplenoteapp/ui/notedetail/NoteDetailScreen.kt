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
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isChecklist by remember { mutableStateOf(false) }
    var checklistItems by remember { mutableStateOf(listOf<String>()) } // Initialized as empty

    // Parse noteIdString and load note
    LaunchedEffect(key1 = noteIdString, key2 = viewModel) {
        currentNoteId = if (noteIdString == "-1" || noteIdString == null) {
            viewModel.clearSelectedNote() // Clear for new note
            null
        } else {
            noteIdString.toLongOrNull()?.also {
                viewModel.getNoteById(it)
            }
        }
        // Reset local fields for new note scenario explicitly
        if (currentNoteId == null) {
            title = ""
            content = ""
            isChecklist = false
            checklistItems = listOf("") // Start with one empty item for new checklist
        }
    }

    // Update local UI state when selectedNoteState changes (e.g., after loading)
    LaunchedEffect(selectedNoteState) {
        if (selectedNoteState is NoteUiState.Success) {
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

    // Handle action results (save, delete)
    LaunchedEffect(actionResultState) {
        when (val result = actionResultState) {
            is NoteUiState.Success -> {
                if (result.data == Unit && (currentNoteId != null || noteIdString == "-1")) { // Check if it's a relevant success
                    // Only navigate up if an action (add/update/delete) was successful
                    // For delete or save, navigate up.
                    // Check if the action was a save (add/update) or delete to decide navigation.
                    // This logic might need refinement based on specific actions.
                    // For now, assuming any action success leads to navigateUp.
                    // onNavigateUp() // This might be too broad, consider specific flags
                }
            }
            is NoteUiState.Error -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = result.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
            is NoteUiState.Loading -> { /* Optionally show loading indicator for action */ }
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (currentNoteId == null) "New Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (currentNoteId != null) {
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
                        val noteToSave = Note(
                            id = currentNoteId, // Will be null for new notes
                            title = title.trim(),
                            content = finalContent.trim(),
                            isChecklist = isChecklist,
                            // timestamp, created_at, updated_at will be handled by server/repository
                        )
                        if (currentNoteId == null) {
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
                if (currentNoteId != null) { // Only show loading if we are expecting a note
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
                        onChecklistItemsChange = { checklistItems = it }
                    )
                }
            }
            is NoteUiState.Success -> {
                // Data is already set to local state vars via LaunchedEffect(selectedNoteState)
                NoteEditorContent(
                    modifier = Modifier.padding(paddingValues),
                    title = title,
                    onTitleChange = { title = it },
                    content = content,
                    onContentChange = { content = it },
                    isChecklist = isChecklist,
                    onIsChecklistChange = { isChecklist = it },
                    checklistItems = checklistItems,
                    onChecklistItemsChange = { checklistItems = it }
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
    onChecklistItemsChange: (List<String>) -> Unit
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
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
