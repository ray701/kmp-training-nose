package org.example.kmp.training.feature.location.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

actual fun createDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val databaseFile = File(System.getProperty("user.home"), DATABASE_NAME)
    return Room.databaseBuilder<AppDatabase>(
        name = databaseFile.absolutePath,
    )
}

private const val DATABASE_NAME = ".kmp-training-registered-locations.db"
