package br.com.turismooffline.data

data class TouristSpot(
    val id: Long = 0,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val imageBytes: ByteArray? = null,
    val imageUri: String? = null
)
