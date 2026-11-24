package org.example.kmp.training.feature.location.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual fun createDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val directory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSApplicationSupportDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    val directoryPath = requireNotNull(directory?.path) {
        "Application Support directory is unavailable."
    }
    return Room.databaseBuilder<AppDatabase>(
        name = "$directoryPath/$DATABASE_NAME",
    )
}

private const val DATABASE_NAME = "registered_locations.db"
