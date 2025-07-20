package com.utn.sistemamonitoreodispositivos

data class SensorData(
    val id: Long,
    val deviceId: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)