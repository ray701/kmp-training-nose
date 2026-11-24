package org.example.kmp.training.feature.location.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "registered_locations")
data class RegisteredLocationEntity(
    @PrimaryKey
    val id: Int = SINGLE_LOCATION_ID,
    val latitude: Double,
    val longitude: Double,
) {
    companion object {
        const val SINGLE_LOCATION_ID = 1
    }
}
