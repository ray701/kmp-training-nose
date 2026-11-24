package org.example.kmp.training.feature.location.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface RegisteredLocationDao {
    @Query("SELECT * FROM registered_locations WHERE id = 1 LIMIT 1")
    suspend fun getRegisteredLocation(): RegisteredLocationEntity?

    @Upsert
    suspend fun upsert(location: RegisteredLocationEntity)
}
