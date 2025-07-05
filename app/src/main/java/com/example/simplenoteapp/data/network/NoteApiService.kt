package com.example.simplenoteapp.data.network

import android.util.Log
import com.example.simplenoteapp.data.Note // Changed import from .model.Note to .data.Note
import com.example.simplenoteapp.data.NoteDto
import com.example.simplenoteapp.data.toDto
import com.example.simplenoteapp.data.toEntity
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Wrapper classes for GET responses, matching backend structure
@Serializable
data class GetNotesResponse(val notes: List<NoteDto>)

@Serializable
data class GetNoteResponse(val note: NoteDto)


class NoteApiService {
    companion object {
        // TODO: Move this to a secure location like encrypted SharedPreferences or BuildConfig
        private const val API_KEY = "K/wp6pZDI8uGWo1HrD/Am1JHSoJTuzPG1FaC+7Fq7EY="
    }
    
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

    // Updated BASE_URL to current Vercel deployment
    private val BASE_URL = "https://vercel-backend-nda919zgv-nd-tungs-projects.vercel.app/api/notes"
    private val SINGLE_NOTE_URL = "https://vercel-backend-nda919zgv-nd-tungs-projects.vercel.app/api/note"

    suspend fun getNotes(): List<Note> {
        Log.d("NoteApiService", "Fetching notes from: $BASE_URL")
        val response = client.get(BASE_URL) {
            header("x-api-key", API_KEY)
        }
        Log.d("NoteApiService", "Get Notes Raw Response: ${response.status.value}")
        if (response.status == HttpStatusCode.OK) {
            val notesResponse = response.body<GetNotesResponse>()
            return notesResponse.notes.map { it.toEntity() }
        } else {
            // Handle error, e.g., throw exception or return empty list
            Log.e("NoteApiService", "Error fetching notes: ${response.status.value}")
            return emptyList()
        }
    }

    suspend fun getNoteById(serverId: Long): Note? {
        Log.d("NoteApiService", "Fetching note by ID: $serverId")
        val response = client.get("$SINGLE_NOTE_URL?id=$serverId") {
            header("x-api-key", API_KEY)
        }
        Log.d("NoteApiService", "Get Note By ID Raw Response: ${response.status.value}")
        return when (response.status) {
            HttpStatusCode.OK -> {
                val noteResponse = response.body<GetNoteResponse>()
                noteResponse.note.toEntity()
            }
            HttpStatusCode.NotFound -> {
                Log.d("NoteApiService", "Note not found with ID: $serverId")
                null
            }
            else -> {
                Log.e("NoteApiService", "Error fetching note: ${response.status.value}")
                throw Exception("Failed to fetch note: ${response.status.value}")
            }
        }
    }

    suspend fun createNote(note: Note): Note {
        Log.d("NoteApiService", "Creating note - title: ${note.title}, content: ${note.content}")
        // Convert to DTO for API request
        val requestBody = note.toDto()
        val response = client.post(BASE_URL) {
            header("x-api-key", API_KEY)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
        Log.d("NoteApiService", "Create Note Raw Response: ${response.status.value}")
        if (response.status == HttpStatusCode.Created) {
            val createdNoteResponse = response.body<GetNoteResponse>()
            return createdNoteResponse.note.toEntity()
        } else {
            // Handle error
            Log.e("NoteApiService", "Error creating note: ${response.status.value}, Body: ${response.body<String>()}")
            // Consider throwing an exception or returning a specific error object
            throw Exception("Failed to create note: ${response.status.value}")
        }
    }

    suspend fun updateNote(note: Note): Note {
        requireNotNull(note.serverId) { "Note server ID cannot be null for update" }
        Log.d("NoteApiService", "Updating note with ID: ${note.serverId} - title: ${note.title}, content: ${note.content}")
        // Convert to DTO for API request
        val requestBody = note.toDto()
        val response = client.put("$BASE_URL?id=${note.serverId}") { // Use serverId for API call
            header("x-api-key", API_KEY)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
        Log.d("NoteApiService", "Update Note Raw Response: ${response.status.value}")
         if (response.status == HttpStatusCode.OK) {
            val updatedNoteResponse = response.body<GetNoteResponse>()
            return updatedNoteResponse.note.toEntity()
        } else {
            // Handle error
            Log.e("NoteApiService", "Error updating note: ${response.status.value}, Body: ${response.body<String>()}")
            throw Exception("Failed to update note: ${response.status.value}")
        }
    }

    suspend fun deleteNote(serverId: Long) {
        Log.d("NoteApiService", "Deleting note with server ID: $serverId")
        val response = client.delete("$BASE_URL?id=$serverId") { // Use serverId for API call
            header("x-api-key", API_KEY)
        }
        Log.d("NoteApiService", "Delete Note Raw Response: ${response.status.value}")
        if (response.status != HttpStatusCode.OK && response.status != HttpStatusCode.NoContent) { // Backend returns 200 with message
            // Handle error
            Log.e("NoteApiService", "Error deleting note: ${response.status.value}, Body: ${response.body<String>()}")
            throw Exception("Failed to delete note: ${response.status.value}")
        }
        // Success, no content to return or backend returns a message like {"message": "Note deleted successfully"}
    }
}
