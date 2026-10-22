package com.example.bitcount.model

enum class AppTheme(val displayName: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

enum class BinaryGrouping(val displayName: String, val groupSize: Int) {
    OFF("Off", 0),
    FOUR("Groups of 4", 4),
    EIGHT("Groups of 8", 8)
}

data class AppSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val dynamicColor: Boolean = true,
    val hapticFeedback: Boolean = true,
    val animations: Boolean = true,
    val confirmDelete: Boolean = true,
    val groupBinaryBits: BinaryGrouping = BinaryGrouping.FOUR,
    val showBitPositions: Boolean = true,
    val binaryPrefixInCopy: Boolean = false
)
