package org.example.kmp.training.feature.location.data.local

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RegisteredLocationDaoTest {
    @Test
    fun upsert_replacesSingleLocationAndSurvivesReopen() = runTest {
        val databaseFile = File.createTempFile("registered-locations", ".db")
        databaseFile.delete()

        try {
            val firstDatabase = openDatabase(databaseFile)
            try {
                val database = firstDatabase
                val dao = database.registeredLocationDao()
                dao.upsert(RegisteredLocationEntity(latitude = 35.68, longitude = 139.76))
                dao.upsert(RegisteredLocationEntity(latitude = 34.69, longitude = 135.50))
            } finally {
                firstDatabase.close()
            }

            val reopenedDatabase = openDatabase(databaseFile)
            try {
                val database = reopenedDatabase
                assertEquals(
                    RegisteredLocationEntity(latitude = 34.69, longitude = 135.50),
                    database.registeredLocationDao().getRegisteredLocation(),
                )
            } finally {
                reopenedDatabase.close()
            }
        } finally {
            databaseFile.delete()
            File("${databaseFile.absolutePath}-wal").delete()
            File("${databaseFile.absolutePath}-shm").delete()
        }
    }

    private fun openDatabase(databaseFile: File): AppDatabase =
        Room.databaseBuilder<AppDatabase>(name = databaseFile.absolutePath)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
}
