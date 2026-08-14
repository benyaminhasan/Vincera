package com.example.vincera

data class WorkoutEntryModel(
    var id: String = "",
    var plan: String = "",
    var uebung: String = "",
    var gewicht: Double = 0.0,
    var wiederholungen: Long = 0,
    var saetze: Long = 0,
    var timestamp: Long = 0
)