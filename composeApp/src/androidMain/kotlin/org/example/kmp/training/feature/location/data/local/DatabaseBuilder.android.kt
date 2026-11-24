package org.example.kmp.training.feature.location.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import org.example.kmp.training.feature.location.AndroidAppContext

actual fun createDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val context = AndroidAppContext.context
    val databaseFile = context.getDatabasePath(DATABASE_NAME)
    return Room.databaseBuilder<AppDatabase>(
        context = context,
        name = databaseFile.absolutePath,
    )
}

private const val DATABASE_NAME = "registered_locations.db"
