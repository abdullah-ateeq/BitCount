package com.example.bitcount.model

import java.math.BigDecimal

data class BitCountResult(
    val originalInput: String,
    val normalizedDecimal: String,
    val binary: String,
    val octal: String,
    val hexadecimal: String,

    val naturalBitLength: Int,
    val analyzedBitLength: Int,

    val oneBitCount: Int,
    val zeroBitCount: Int,

    val highestSetBit: Int?,
    val lowestSetBit: Int?,

    val setBitPositions: List<Int>,
    val clearBitPositions: List<Int>,

    val bitDensityPercent: BigDecimal,

    val bitWidthMode: BitWidthMode,

    val calculatedAt: Long,
    val isNegative: Boolean = false,
    val magnitudeBinary: String = binary
)
