package org.example.kmp.training.feature.location.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

expect fun createDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>

fun createAppDatabase(): AppDatabase = createDatabaseBuilder()
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.Default)
    .build()
