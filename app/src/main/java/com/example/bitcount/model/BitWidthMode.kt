package com.example.bitcount.model

sealed class BitWidthMode {
    data object Natural : BitWidthMode()
    data class Fixed(val width: Int) : BitWidthMode()

    fun displayName(): String = when (this) {
        is Natural -> "Natural"
        is Fixed -> "${width}-bit"
    }

    companion object {
        val STANDARD_WIDTHS = listOf(4, 8, 16, 32, 64)
    }
}
