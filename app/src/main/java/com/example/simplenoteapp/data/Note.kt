package com.example.simplenoteapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable // Added for Ktor JSON serialization
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey // Removed autoGenerate = true, ID will come from server or be null
    val id: Long? = null, // Changed from String? to Long? to match backend ID type
    val title: String = "",
    val content: String = "",
    val isChecklist: Boolean = false, // Local-only field
    val timestamp: Long = System.currentTimeMillis(), // Local timestamp, might be superseded by server timestamps
    val created_at: String? = null, // Field from backend
    val updated_at: String? = null  // Field from backend
)
