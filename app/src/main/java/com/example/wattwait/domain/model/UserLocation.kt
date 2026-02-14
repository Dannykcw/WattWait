package com.example.wattwait.domain.model

data class UserLocation(
    val zipCode: String?,
    val latitude: Double?,
    val longitude: Double?,
    val address: String? = null,
    val isGpsLocation: Boolean = false
) {
    val isValid: Boolean
        get() = !zipCode.isNullOrBlank() || (latitude != null && longitude != null)

    val displayText: String
        get() = when {
            !address.isNullOrBlank() -> address
            !zipCode.isNullOrBlank() -> "ZIP: $zipCode"
            latitude != null && longitude != null -> "GPS Location"
            else -> "Not set"
        }
}
