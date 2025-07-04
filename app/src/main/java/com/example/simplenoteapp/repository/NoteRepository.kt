package com.example.simplenoteapp.repository

import com.example.simplenoteapp.data.network.NoteApiService
import com.example.simplenoteapp.model.Note

class NoteRepository(private val apiService: NoteApiService) {

    suspend fun getNotes(): List<Note> {
        return apiService.getNotes()
    }

    suspend fun createNote(note: Note): Note {
        return apiService.createNote(note)
    }

    suspend fun updateNote(note: Note): Note {
        return apiService.updateNote(note)
    }

    suspend fun deleteNote(id: String) {
        apiService.deleteNote(id)
    }
}