package com.example.simplenoteapp.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseNoteService {

    private val db = FirebaseFirestore.getInstance()
    private val notesCollection = db.collection("notes")

    fun getNotes(): Flow<List<Note>> = callbackFlow {
        val subscription = notesCollection.addSnapshotListener {
            snapshot, _ ->
            if (snapshot != null) {
                val notes = snapshot.toObjects(Note::class.java)
                trySend(notes).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }

    suspend fun getNoteById(id: String): Note? {
        return notesCollection.document(id).get().await().toObject(Note::class.java)
    }

    suspend fun addNote(note: Note) {
        val documentRef = if (note.id == null) {
            notesCollection.document()
        } else {
            notesCollection.document(note.id)
        }
        documentRef.set(note.copy(id = documentRef.id)).await()
    }

    suspend fun updateNote(note: Note) {
        notesCollection.document(note.id!!).set(note).await()
    }

    suspend fun deleteNote(note: Note) {
        notesCollection.document(note.id!!).delete().await()
    }
}