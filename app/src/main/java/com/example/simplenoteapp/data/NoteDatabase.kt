package com.example.simplenoteapp.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(
    entities = [Note::class], 
    version = 4, // Increment version for new sync-related fields
    exportSchema = false
)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    
    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null
        
        fun getDatabase(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_database"
                )
                .fallbackToDestructiveMigration() // This will recreate tables on version change
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}