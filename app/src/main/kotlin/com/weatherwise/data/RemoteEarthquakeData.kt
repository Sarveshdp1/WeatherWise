package com.weatherwise.data

// JSON (GeoJSON) that the USGS earthquake service sends us.

data class RemoteEarthquakeData(
    val features: List<EarthquakeFeatureRemote>? = null
)

data class EarthquakeFeatureRemote(
    val properties: EarthquakePropertiesRemote? = null,
    val geometry: EarthquakeGeometryRemote? = null
)

data class EarthquakePropertiesRemote(
    val mag: Double? = null,       // magnitude
    val place: String? = null,     // e.g. "85 km SW of Pune, India"
    val time: Long? = null         // time of the quake in milliseconds since 1970
)

data class EarthquakeGeometryRemote(
    // Order is [longitude, latitude, depth]
    val coordinates: List<Double>? = null
)
