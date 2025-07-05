package com.example.simplenoteapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

// Room Entity (for local database)
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val isChecklist: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val serverId: Long? = null, // Server ID from backend
    val isSynced: Boolean = false, // Whether this note is synced with cloud
    val needsSync: Boolean = false, // Whether this note has local changes that need syncing
    val lastSyncTime: Long = 0L // When this note was last synced
)

// Network Model (for API communication)
@Serializable
data class NoteDto(
    val id: Long? = null,
    val title: String = "",
    val content: String = "",
    val isChecklist: Boolean = false, // Added to match backend
    val created_at: String? = null,
    val updated_at: String? = null
)

// Extension functions for conversion
fun Note.toDto(): NoteDto = NoteDto(
    id = serverId,
    title = title,
    content = content,
    isChecklist = isChecklist
)

fun NoteDto.toEntity(): Note = Note(
    title = title,
    content = content,
    isChecklist = isChecklist,
    serverId = id,
    isSynced = true, // Coming from server means it's synced
    needsSync = false,
    lastSyncTime = System.currentTimeMillis()
)
