package org.example.kmp.training.feature.location.data.repository

import dev.zacsweers.metro.Inject
import org.example.kmp.training.feature.location.data.local.RegisteredLocationDao
import org.example.kmp.training.feature.location.data.local.RegisteredLocationEntity
import org.example.kmp.training.feature.location.domain.model.Location
import org.example.kmp.training.feature.location.domain.repository.RegisteredLocationRepository

@Inject
class RoomRegisteredLocationRepository(
    private val dao: RegisteredLocationDao,
) : RegisteredLocationRepository {
    override suspend fun getRegisteredLocation(): Location? =
        dao.getRegisteredLocation()?.let { entity ->
            Location(
                latitude = entity.latitude,
                longitude = entity.longitude,
            )
        }

    override suspend fun saveRegisteredLocation(location: Location) {
        dao.upsert(
            RegisteredLocationEntity(
                latitude = location.latitude,
                longitude = location.longitude,
            ),
        )
    }
}
