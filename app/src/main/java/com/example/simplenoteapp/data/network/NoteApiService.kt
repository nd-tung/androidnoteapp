package com.example.simplenoteapp.data.network

import android.util.Log
import com.example.simplenoteapp.data.Note // Changed import from .model.Note to .data.Note
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

// Data classes for request/response mapping if Note class has local-only fields
// For now, assuming Note class can be directly used or adapted.
// If Note has fields like `isChecklist` not present in backend,
// we might need specific DTOs for create/update.

data class CreateNoteRequest(val title: String, val content: String)
data class UpdateNoteRequest(val title: String, val content: String)

// Wrapper classes for GET responses, matching backend structure
data class GetNotesResponse(val notes: List<Note>)
data class GetNoteResponse(val note: Note)


class NoteApiService {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true // Important for backend adding fields like created_at, updated_at
                prettyPrint = true
                isLenient = true
                // explicitNulls = false // Consider if needed based on API behavior with nulls
            })
        }
    }

    // Corrected BASE_URL from vercel-backend/README.md
    private val BASE_URL = "https://vercel-backend-inky-xi.vercel.app/api/notes"

    suspend fun getNotes(): List<Note> {
        Log.d("NoteApiService", "Fetching notes from: $BASE_URL")
        val response = client.get(BASE_URL)
        Log.d("NoteApiService", "Get Notes Raw Response: ${response.status.value}")
        if (response.status == HttpStatusCode.OK) {
            val notesResponse = response.body<GetNotesResponse>()
            return notesResponse.notes
        } else {
            // Handle error, e.g., throw exception or return empty list
            Log.e("NoteApiService", "Error fetching notes: ${response.status.value}")
            return emptyList()
        }
    }

    suspend fun createNote(note: Note): Note {
        Log.d("NoteApiService", "Creating note - title: ${note.title}, content: ${note.content}")
        // Backend expects only title and content for creation
        val requestBody = CreateNoteRequest(title = note.title, content = note.content)
        val response = client.post(BASE_URL) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
        Log.d("NoteApiService", "Create Note Raw Response: ${response.status.value}")
        if (response.status == HttpStatusCode.Created) {
            val createdNoteResponse = response.body<GetNoteResponse>()
            return createdNoteResponse.note
        } else {
            // Handle error
            Log.e("NoteApiService", "Error creating note: ${response.status.value}, Body: ${response.body<String>()}")
            // Consider throwing an exception or returning a specific error object
            throw Exception("Failed to create note: ${response.status.value}")
        }
    }

    suspend fun updateNote(note: Note): Note {
        requireNotNull(note.id) { "Note ID cannot be null for update" }
        Log.d("NoteApiService", "Updating note with ID: ${note.id} - title: ${note.title}, content: ${note.content}")
        // Backend expects only title and content for update
        val requestBody = UpdateNoteRequest(title = note.title, content = note.content)
        val response = client.put("$BASE_URL?id=${note.id}") { // ID as query parameter
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
        Log.d("NoteApiService", "Update Note Raw Response: ${response.status.value}")
         if (response.status == HttpStatusCode.OK) {
            val updatedNoteResponse = response.body<GetNoteResponse>()
            return updatedNoteResponse.note
        } else {
            // Handle error
            Log.e("NoteApiService", "Error updating note: ${response.status.value}, Body: ${response.body<String>()}")
            throw Exception("Failed to update note: ${response.status.value}")
        }
    }

    suspend fun deleteNote(noteId: String) {
        Log.d("NoteApiService", "Deleting note with ID: $noteId")
        val response = client.delete("$BASE_URL?id=$noteId") // ID as query parameter, no request body
        Log.d("NoteApiService", "Delete Note Raw Response: ${response.status.value}")
        if (response.status != HttpStatusCode.OK && response.status != HttpStatusCode.NoContent) { // Backend returns 200 with message
            // Handle error
            Log.e("NoteApiService", "Error deleting note: ${response.status.value}, Body: ${response.body<String>()}")
            throw Exception("Failed to delete note: ${response.status.value}")
        }
        // Success, no content to return or backend returns a message like {"message": "Note deleted successfully"}
    }
}
