package com.example.wattwait.domain.model

enum class ApplianceType(
    val displayName: String,
    val averageKwhPerUse: Double,
    val iconName: String
) {
    OVEN("Oven/Range", 3.0, "oven"),
    WASHER("Washing Machine", 2.0, "washer"),
    DRYER("Clothes Dryer", 3.0, "dryer"),
    DISHWASHER("Dishwasher", 1.8, "dishwasher"),
    AIR_CONDITIONER("Air Conditioner", 3.5, "ac"),
    HEATER("Space Heater", 1.5, "heater"),
    WATER_HEATER("Water Heater", 4.5, "water_heater"),
    REFRIGERATOR("Refrigerator", 0.15, "fridge"),
    MICROWAVE("Microwave", 1.2, "microwave"),
    TV("Television", 0.1, "tv"),
    VACUUM("Vacuum Cleaner", 1.0, "vacuum"),
    POOL_PUMP("Pool Pump", 2.5, "pool"),
    EV_CHARGER("EV Charger", 7.5, "ev"),
    THERMOSTAT("Smart Thermostat", 0.5, "thermostat"),
    LIGHTING("Smart Lighting", 0.1, "lighting"),
    GENERIC("Generic Appliance", 1.5, "generic");

    companion object {
        fun fromString(value: String): ApplianceType {
            return entries.find { it.name == value } ?: GENERIC
        }
    }
}
