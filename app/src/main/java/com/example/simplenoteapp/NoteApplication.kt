package com.example.simplenoteapp

import android.app.Application
import com.example.simplenoteapp.data.NoteDatabase
import com.example.simplenoteapp.data.NoteRepository
import com.example.simplenoteapp.data.network.NoteApiService

class NoteApplication : Application() {

    // Database instance
    private val database by lazy { NoteDatabase.getDatabase(this) }

    // DAO instance
    private val noteDao by lazy { database.noteDao() }

    // ApiService instance
    private val noteApiService by lazy { NoteApiService() }

    // Unified NoteRepository instance
    val noteRepository: NoteRepository by lazy {
        NoteRepository(noteDao, noteApiService)
    }

    // FirebaseNoteService is no longer needed here if we are fully switching
    // val firebaseNoteService: FirebaseNoteService by lazy { FirebaseNoteService() }
}
