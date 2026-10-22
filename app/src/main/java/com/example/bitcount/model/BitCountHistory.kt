package com.example.bitcount.model

data class BitCountHistory(
    val id: String,
    val inputBase: InputBase,
    val input: String,
    val bitWidth: BitWidthMode,
    val savedAt: Long
)
