package com.example.simplenoteapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.simplenoteapp.data.Note
import com.example.simplenoteapp.data.FirebaseNoteService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class NoteViewModel(private val firebaseNoteService: FirebaseNoteService) : ViewModel() {

    fun getNotes(): Flow<List<Note>> = firebaseNoteService.getNotes()

    fun searchNotes(searchQuery: String): Flow<List<Note>> = firebaseNoteService.getNotes() // Firebase doesn't have direct search, will filter locally or implement server-side

    fun getNoteById(id: String): Flow<Note?> = flow {
        emit(firebaseNoteService.getNoteById(id))
    }

    fun addNote(note: Note) {
        viewModelScope.launch {
            firebaseNoteService.addNote(note)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            firebaseNoteService.updateNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            firebaseNoteService.deleteNote(note)
        }
    }
}

class NoteViewModelFactory(private val firebaseNoteService: FirebaseNoteService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(firebaseNoteService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
