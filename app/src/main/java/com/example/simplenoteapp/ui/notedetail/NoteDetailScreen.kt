package com.example.simplenoteapp.ui.notedetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
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
fun NoteDetailScreen(
    noteIdString: String?,
    onNavigateUp: () -> Unit,
    viewModel: NoteViewModel,
    drawerState: DrawerState // Added DrawerState parameter
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
    var checklistItems by remember { mutableStateOf(listOf<String>()) }
    var showSyncMenu by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = noteIdString) {
        if (noteIdString == "-1" || noteIdString == null) {
            isNewNote = true
            currentNoteId = null
            viewModel.clearSelectedNote()
            title = ""
            content = ""
            isChecklist = false
            checklistItems = listOf("")
        } else {
            isNewNote = false
            noteIdString.toLongOrNull()?.let { id ->
                currentNoteId = id
                viewModel.getNoteById(id)
            }
        }
    }

    LaunchedEffect(selectedNoteState) {
        if (selectedNoteState is NoteUiState.Success && !isNewNote) {
            (selectedNoteState as NoteUiState.Success<Note?>).data?.let { note ->
                title = note.title
                content = note.content
                isChecklist = note.isChecklist
                checklistItems = if (note.isChecklist && note.content.isNotEmpty()) {
                    note.content.split("\n")
                } else if (note.isChecklist) {
                    listOf("")
                } else {
                    listOf()
                }
            }
        }
    }

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
            else -> { /* No explicit success message needed here as navigation handles it */ }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (isNewNote) "Add New Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to notes list")
                    }
                },
                actions = {
                    if (!isNewNote && currentNoteId != null) {
                        Box {
                            IconButton(onClick = { showSyncMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Sync options")
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
                                            currentNote.serverId.let { serverId ->
                                                viewModel.syncNoteFromCloud(serverId)
                                            }
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Info, contentDescription = null)
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Delete Note") },
                                    onClick = {
                                        showSyncMenu = false
                                        (selectedNoteState as? NoteUiState.Success<Note?>)?.data?.let { noteToDelete ->
                                            viewModel.deleteNote(noteToDelete)
                                            onNavigateUp()
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete note")
                                    }
                                )
                            }
                        }
                    }
                    IconButton(onClick = {
                        val finalContent = if (isChecklist) {
                            checklistItems.joinToString("\n")
                        } else {
                            content
                        }
                        val noteToSave = if (!isNewNote && currentNoteId != null) {
                            val currentLoadedNote = (selectedNoteState as? NoteUiState.Success<Note?>)?.data
                            Note(
                                id = currentNoteId!!,
                                title = title.trim(),
                                content = finalContent.trim(),
                                isChecklist = isChecklist,
                                serverId = currentLoadedNote?.serverId,
                                isSynced = false,
                                needsSync = true,
                                lastSyncTime = currentLoadedNote?.lastSyncTime ?: 0L
                            )
                        } else {
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
                        onNavigateUp()
                    }) {
                        Icon(Icons.Default.Done, contentDescription = "Save note")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        val showLoading = selectedNoteState is NoteUiState.Loading && !isNewNote && currentNoteId != null

        if (showLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            NoteEditorContent(
                modifier = Modifier.padding(paddingValues),
                title = title,
                onTitleChange = { title = it },
                content = content,
                onContentChange = { content = it },
                isChecklist = isChecklist,
                onIsChecklistChange = {
                    isChecklist = it
                    if (it && checklistItems.isEmpty()) {
                        checklistItems = listOf("")
                    } else if (!it) {
                        checklistItems = emptyList()
                    }
                },
                checklistItems = checklistItems,
                onChecklistItemsChange = { checklistItems = it },
                note = (selectedNoteState as? NoteUiState.Success<Note?>)?.data
            )
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
    note: Note? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp) // Consistent padding
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { 
                Text(
                    "Title",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ) 
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            ), // Consistent text styling
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next, capitalization = KeyboardCapitalization.Sentences)
        )
        Spacer(modifier = Modifier.height(20.dp)) // Consistent spacing

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp), // Consistent padding
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Checklist mode",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            ) // Consistent text styling
            Switch(
                checked = isChecklist,
                onCheckedChange = onIsChecklistChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
        Spacer(modifier = Modifier.height(20.dp)) // Consistent spacing

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
                        },
                        onDeleteItem = {
                            val newList = checklistItems.toMutableList()
                            newList.removeAt(index)
                            // If the list becomes empty after deletion, add a new empty item
                            // to allow the user to continue adding items easily.
                            if (newList.isEmpty()) {
                                onChecklistItemsChange(listOf(""))
                            } else {
                                onChecklistItemsChange(newList)
                            }
                        }
                    )
                }
                item {
                    Button(
                        onClick = { onChecklistItemsChange(checklistItems + "[ ] ") },
                        modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add checklist item")
                        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                        Text("Add Item")
                    }
                }
            }
        } else {
            OutlinedTextField(
                value = content,
                onValueChange = onContentChange,
                label = { 
                    Text(
                        "Write your note...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2f
                ), // Consistent text styling with better line height
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.Sentences)
            )
        }
    }
}

@Composable
fun ChecklistItemEditor(
    item: String,
    onItemChange: (String) -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onDeleteItem: () -> Unit
) {
    val isChecked = item.startsWith("[x] ")
    val text = item.removePrefix("[x] ").removePrefix("[ ] ")

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
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
            singleLine = true,
            placeholder = { Text("List item") }
        )
        IconButton(onClick = onDeleteItem) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete checklist item")
        }
    }
}

