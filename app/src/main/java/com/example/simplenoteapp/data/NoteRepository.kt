package com.example.simplenoteapp.data

import android.util.Log
import com.example.simplenoteapp.data.network.NoteApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.io.IOException

// Consider defining a more specific exception for repository errors
class NoteRepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause)

class NoteRepository(
    private val noteDao: NoteDao,
    private val noteApiService: NoteApiService
) {

    // Provides a Flow of notes from the local database.
    // The UI collects this Flow.
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()

    // Fetches notes from the backend and updates the local database.
    // This should be called strategically (e.g., on app start, pull-to-refresh).
    suspend fun refreshNotes() {
        try {
            val remoteNotes = noteApiService.getNotes()
            Log.d("NoteRepository", "Fetched ${remoteNotes.size} notes from API")
            // We could also implement logic here to delete local notes that are no longer on the server
            // For now, just upsert all fetched notes.
            remoteNotes.forEach { note ->
                noteDao.upsertNote(note)
            }
            Log.d("NoteRepository", "Upserted ${remoteNotes.size} notes into local DB")
        } catch (e: Exception) { // Catching general Exception, but could be more specific (e.g., Ktor exceptions, IOException)
            Log.e("NoteRepository", "Failed to refresh notes: ${e.message}", e)
            throw NoteRepositoryException("Failed to refresh notes from server.", e)
        }
    }

    // Gets a single note by ID from the local database.
    fun getNoteById(id: Long): Flow<Note?> = noteDao.getNoteById(id) // Changed from Int to Long

    // Adds a new note:
    // 1. Creates it via the API.
    // 2. If successful, upserts the returned note (with server ID and timestamps) into the local DB.
    suspend fun addNote(note: Note): Note {
        try {
            // Note object for creation might not have an ID yet.
            // API service's createNote should handle a Note object and map to CreateNoteRequest
            val createdNote = noteApiService.createNote(note)
            Log.d("NoteRepository", "Note created via API: $createdNote")
            noteDao.upsertNote(createdNote) // Upsert the note with server-generated ID and timestamps
            Log.d("NoteRepository", "Upserted created note into local DB: $createdNote")
            return createdNote
        } catch (e: Exception) {
            Log.e("NoteRepository", "Failed to add note: ${e.message}", e)
            throw NoteRepositoryException("Failed to add note.", e)
        }
    }

    // Updates an existing note:
    // 1. Updates it via the API.
    // 2. If successful, upserts the returned note into the local DB.
    suspend fun updateNote(note: Note): Note {
        if (note.serverId == null) {
            Log.e("NoteRepository", "Cannot update note with null server ID.")
            throw NoteRepositoryException("Cannot update note with null server ID.")
        }
        try {
            val updatedNote = noteApiService.updateNote(note)
            Log.d("NoteRepository", "Note updated via API: $updatedNote")
            noteDao.upsertNote(updatedNote)
            Log.d("NoteRepository", "Upserted updated note into local DB: $updatedNote")
            return updatedNote
        } catch (e: Exception) {
            Log.e("NoteRepository", "Failed to update note: ${e.message}", e)
            throw NoteRepositoryException("Failed to update note with server ID ${note.serverId}.", e)
        }
    }

    // Deletes a note:
    // 1. Deletes it via the API.
    // 2. If successful, deletes it from the local DB.
    suspend fun deleteNote(note: Note) {
        if (note.serverId == null) {
            Log.e("NoteRepository", "Cannot delete note with null server ID.")
            throw NoteRepositoryException("Cannot delete note with null server ID.")
        }
        try {
            noteApiService.deleteNote(note.serverId) // Pass serverId to API
            Log.d("NoteRepository", "Note deleted via API, server ID: ${note.serverId}")
            noteDao.deleteNoteById(note.id) // Delete by local ID
            Log.d("NoteRepository", "Deleted note from local DB, local ID: ${note.id}")
        } catch (e: Exception) {
            Log.e("NoteRepository", "Failed to delete note: ${e.message}", e)
            throw NoteRepositoryException("Failed to delete note with server ID ${note.serverId}.", e)
        }
    }

    // Example of how one might fetch a note and ensure it's up-to-date from server
    // This is a more complex scenario, just illustrating.
    suspend fun getFreshNoteById(id: Long): Note? {
        refreshNotes() // Or a more targeted refresh for a single item if API supports it
        return noteDao.getNoteById(id).firstOrNull()
    }

    // Manual sync methods for individual notes
    suspend fun syncNoteToCloud(note: Note): Note {
        if (note.serverId == null) {
            // Note doesn't exist on server, create it
            return createNoteOnCloud(note)
        } else {
            // Note exists on server, update it
            return updateNoteOnCloud(note)
        }
    }

    suspend fun syncNoteFromCloud(serverId: Long): Note? {
        try {
            // Use the new single note endpoint for efficiency
            val cloudNote = noteApiService.getNoteById(serverId)
            return if (cloudNote != null) {
                // Update local note with cloud data
                val syncedNote = cloudNote.copy(
                    isSynced = true,
                    needsSync = false,
                    lastSyncTime = System.currentTimeMillis()
                )
                noteDao.upsertNote(syncedNote)
                Log.d("NoteRepository", "Note synced from cloud: $syncedNote")
                syncedNote
            } else {
                Log.w("NoteRepository", "Note not found in cloud with server ID: $serverId")
                null
            }
        } catch (e: Exception) {
            Log.e("NoteRepository", "Failed to sync note from cloud: ${e.message}", e)
            throw NoteRepositoryException("Failed to sync note from cloud.", e)
        }
    }

    private suspend fun createNoteOnCloud(note: Note): Note {
        try {
            val createdNote = noteApiService.createNote(note)
            val syncedNote = createdNote.copy(
                id = note.id, // Keep local ID
                isSynced = true,
                needsSync = false,
                lastSyncTime = System.currentTimeMillis()
            )
            noteDao.upsertNote(syncedNote)
            Log.d("NoteRepository", "Note synced to cloud: $syncedNote")
            return syncedNote
        } catch (e: Exception) {
            // Mark note as needing sync
            val needsSyncNote = note.copy(needsSync = true, isSynced = false)
            noteDao.upsertNote(needsSyncNote)
            Log.e("NoteRepository", "Failed to sync note to cloud: ${e.message}", e)
            throw NoteRepositoryException("Failed to sync note to cloud.", e)
        }
    }

    private suspend fun updateNoteOnCloud(note: Note): Note {
        if (note.serverId == null) {
            throw NoteRepositoryException("Cannot update note with null server ID.")
        }
        try {
            val updatedNote = noteApiService.updateNote(note)
            val syncedNote = updatedNote.copy(
                id = note.id, // Keep local ID
                isSynced = true,
                needsSync = false,
                lastSyncTime = System.currentTimeMillis()
            )
            noteDao.upsertNote(syncedNote)
            Log.d("NoteRepository", "Note updated on cloud: $syncedNote")
            return syncedNote
        } catch (e: Exception) {
            // Mark note as needing sync
            val needsSyncNote = note.copy(needsSync = true, isSynced = false)
            noteDao.upsertNote(needsSyncNote)
            Log.e("NoteRepository", "Failed to update note on cloud: ${e.message}", e)
            throw NoteRepositoryException("Failed to update note on cloud.", e)
        }
    }
}