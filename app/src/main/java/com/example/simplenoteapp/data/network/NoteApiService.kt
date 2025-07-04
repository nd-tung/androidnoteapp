package com.example.simplenoteapp.data.network

import android.util.Log
import com.example.simplenoteapp.model.Note
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class NoteApiService {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }

    private val BASE_URL = "https://simplenote-i7qo4bkx9-nd-tungs-projects.vercel.app/api/notes" // Development Vercel function URL

    suspend fun getNotes(): List<Note> {
        Log.d("NoteApiService", "Fetching notes from: $BASE_URL")
        val response = client.get(BASE_URL)
        Log.d("NoteApiService", "Get Notes Response: ${response.status.value}")
        return response.body()
    }

    suspend fun createNote(note: Note): Note {
        Log.d("NoteApiService", "Creating note: $note")
        val response = client.post(BASE_URL) {
            contentType(ContentType.Application.Json)
            setBody(note)
        }
        Log.d("NoteApiService", "Create Note Response: ${response.status.value}")
        return response.body()
    }

    suspend fun updateNote(note: Note): Note {
        Log.d("NoteApiService", "Updating note: $note")
        val response = client.put(BASE_URL) {
            contentType(ContentType.Application.Json)
            setBody(note)
        }
        Log.d("NoteApiService", "Update Note Response: ${response.status.value}")
        return response.body()
    }

    suspend fun deleteNote(id: String) {
        Log.d("NoteApiService", "Deleting note with ID: $id")
        val response = client.delete(BASE_URL) {
            contentType(ContentType.Application.Json)
            setBody(mapOf("id" to id))
        }
        Log.d("NoteApiService", "Delete Note Response: ${response.status.value}")
    }
}
