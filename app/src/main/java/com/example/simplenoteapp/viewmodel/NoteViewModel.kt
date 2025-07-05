package com.example.simplenoteapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.simplenoteapp.data.Note
import com.example.simplenoteapp.data.NoteRepository // Changed from FirebaseNoteService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Sealed class to represent UI State
sealed class NoteUiState<out T> {
    object Loading : NoteUiState<Nothing>()
    data class Success<T>(val data: T) : NoteUiState<T>()
    data class Error(val message: String) : NoteUiState<Nothing>()
}

class NoteViewModel(private val repository: NoteRepository) : ViewModel() { // Changed to NoteRepository

    private val _notesState = MutableStateFlow<NoteUiState<List<Note>>>(NoteUiState.Loading)
    val notesState: StateFlow<NoteUiState<List<Note>>> = _notesState.asStateFlow()

    private val _selectedNoteState = MutableStateFlow<NoteUiState<Note?>>(NoteUiState.Loading)
    val selectedNoteState: StateFlow<NoteUiState<Note?>> = _selectedNoteState.asStateFlow()

    // For single action results (add, update, delete)
    private val _actionResult = MutableStateFlow<NoteUiState<Unit>>(NoteUiState.Success(Unit)) // Initial state as success or idle
    val actionResult: StateFlow<NoteUiState<Unit>> = _actionResult.asStateFlow()


    init {
        // Set up continuous collection of notes from the repository
        viewModelScope.launch {
            repository.getAllNotes()
                .catch { e ->
                    _notesState.value = NoteUiState.Error(e.message ?: "Failed to load notes")
                }
                .collect { notes ->
                    _notesState.value = NoteUiState.Success(notes)
                }
        }
        // Initial refresh from network
        refreshAllNotes()
    }

    private fun loadNotesFromDatabase() {
        viewModelScope.launch {
            repository.getAllNotes()
                .catch { e ->
                    _notesState.value = NoteUiState.Error(e.message ?: "Failed to load notes")
                }
                .collect { notes ->
                    _notesState.value = NoteUiState.Success(notes)
                }
        }
    }

    fun refreshAllNotes() {
        viewModelScope.launch {
            _notesState.value = NoteUiState.Loading
            try {
                repository.refreshNotes()
                // The flow from getAllNotes will automatically update _notesState via its collector
            } catch (e: Exception) {
                 _notesState.value = NoteUiState.Error(e.message ?: "Failed to refresh notes")
                 // If refresh fails, existing data in flow will still be shown or an empty list if nothing was there
            }
        }
    }


    fun getNoteById(id: Long) { // Changed id type to Long
        viewModelScope.launch {
            _selectedNoteState.value = NoteUiState.Loading
            repository.getNoteById(id)
                .catch { e ->
                    _selectedNoteState.value = NoteUiState.Error(e.message ?: "Failed to load note")
                }
                .collect { note ->
                    _selectedNoteState.value = NoteUiState.Success(note)
                }
        }
    }

    fun clearSelectedNote() {
        _selectedNoteState.value = NoteUiState.Success(null) // Clear selection or set to loading/idle
    }

    fun addNote(note: Note) {
        viewModelScope.launch {
            _actionResult.value = NoteUiState.Loading
            try {
                repository.addNote(note)
                _actionResult.value = NoteUiState.Success(Unit)
                // No need to refresh - the Flow from repository will automatically update
            } catch (e: Exception) {
                _actionResult.value = NoteUiState.Error(e.message ?: "Failed to add note")
            }
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            _actionResult.value = NoteUiState.Loading
            try {
                repository.updateNote(note)
                _actionResult.value = NoteUiState.Success(Unit)
                // No need to refresh - the Flow from repository will automatically update
            } catch (e: Exception) {
                _actionResult.value = NoteUiState.Error(e.message ?: "Failed to update note")
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            _actionResult.value = NoteUiState.Loading
            try {
                repository.deleteNote(note)
                _actionResult.value = NoteUiState.Success(Unit)
                // No need to refresh - the Flow from repository will automatically update
            } catch (e: Exception) {
                _actionResult.value = NoteUiState.Error(e.message ?: "Failed to delete note")
            }
        }
    }

    // Manual sync methods for individual notes
    fun syncNoteToCloud(note: Note) {
        viewModelScope.launch {
            _actionResult.value = NoteUiState.Loading
            try {
                repository.syncNoteToCloud(note)
                _actionResult.value = NoteUiState.Success(Unit)
                // The Flow from repository will automatically update with synced note
            } catch (e: Exception) {
                _actionResult.value = NoteUiState.Error(e.message ?: "Failed to sync note to cloud")
            }
        }
    }

    fun syncNoteFromCloud(serverId: Long) {
        viewModelScope.launch {
            _actionResult.value = NoteUiState.Loading
            try {
                repository.syncNoteFromCloud(serverId)
                _actionResult.value = NoteUiState.Success(Unit)
                // The Flow from repository will automatically update with synced note
            } catch (e: Exception) {
                _actionResult.value = NoteUiState.Error(e.message ?: "Failed to sync note from cloud")
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
class NoteViewModelFactory(private val repository: NoteRepository) : ViewModelProvider.Factory { // Changed to NoteRepository
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            return NoteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
