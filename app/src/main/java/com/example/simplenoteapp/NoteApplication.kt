package com.example.simplenoteapp

import android.app.Application
import com.example.simplenoteapp.data.FirebaseNoteService

class NoteApplication : Application() {
    val firebaseNoteService: FirebaseNoteService by lazy { FirebaseNoteService() }
}
