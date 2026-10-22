package com.example.bitcount.analysis

import com.example.bitcount.model.BitCountResult
import com.example.bitcount.model.BitWidthMode
import com.example.bitcount.model.InputBase
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

class BitCountAnalyzer(
    private val parser: BitCountParser = BitCountParser()
) {

    fun analyze(
        input: String,
        inputBase: InputBase,
        bitWidthMode: BitWidthMode
    ): Result<BitCountResult> {
        val parseResult = parser.parse(input, inputBase)
        if (parseResult.isFailure) {
            return Result.failure(parseResult.exceptionOrNull() ?: IllegalArgumentException("Analysis failed"))
        }

        val parsedValue = parseResult.getOrThrow()
        val isNegative = parsedValue.signum() < 0
        val magnitude = parsedValue.abs()

        val isZero = magnitude == BigInteger.ZERO

        // Natural binary calculations
        val naturalBitLength = if (isZero) 1 else magnitude.bitLength()
        val naturalBinary = if (isZero) "0" else magnitude.toString(2)

        // Validate fixed width if selected
        val analyzedBitLength: Int
        val paddedMagnitude: String

        when (bitWidthMode) {
            is BitWidthMode.Natural -> {
                analyzedBitLength = naturalBitLength
                paddedMagnitude = naturalBinary
            }
            is BitWidthMode.Fixed -> {
                val width = bitWidthMode.width
                if (naturalBitLength > width) {
                    return Result.failure(
                        IllegalArgumentException("This value does not fit within $width bits. Choose a larger width.")
                    )
                }
                analyzedBitLength = width
                paddedMagnitude = if (isZero) {
                    "0".repeat(width)
                } else {
                    naturalBinary.padStart(width, '0')
                }
            }
        }

        // Set and clear bits
        val oneBitCount: Int
        val zeroBitCount: Int
        val highestSetBit: Int?
        val lowestSetBit: Int?
        val setBitPositions = mutableListOf<Int>()
        val clearBitPositions = mutableListOf<Int>()

        if (isZero) {
            oneBitCount = 0
            zeroBitCount = analyzedBitLength
            highestSetBit = null
            lowestSetBit = null
            for (i in (analyzedBitLength - 1) downTo 0) {
                clearBitPositions.add(i)
            }
        } else {
            oneBitCount = magnitude.bitCount()
            zeroBitCount = analyzedBitLength - oneBitCount
            highestSetBit = naturalBitLength - 1
            lowestSetBit = magnitude.lowestSetBit

            for (i in (analyzedBitLength - 1) downTo 0) {
                if (magnitude.testBit(i)) {
                    setBitPositions.add(i)
                } else {
                    clearBitPositions.add(i)
                }
            }
        }

        // Bit Density = (1-Bits / Total Bits) * 100
        val bitDensityPercent = if (analyzedBitLength == 0) {
            BigDecimal.ZERO
        } else {
            val density = BigDecimal(oneBitCount)
                .multiply(BigDecimal(100))
                .divide(BigDecimal(analyzedBitLength), 4, RoundingMode.HALF_UP)
                .stripTrailingZeros()
            if (density.scale() < 0) density.setScale(0) else density
        }

        // Output representations
        val normalizedDecimal = if (isZero) {
            "0"
        } else {
            (if (isNegative) "-" else "") + magnitude.toString(10)
        }

        val finalBinary = (if (isNegative) "-" else "") + paddedMagnitude

        val octal = if (isZero) {
            "0"
        } else {
            (if (isNegative) "-" else "") + magnitude.toString(8)
        }

        val hexadecimal = if (isZero) {
            "0"
        } else {
            (if (isNegative) "-" else "") + magnitude.toString(16).uppercase()
        }

        val result = BitCountResult(
            originalInput = input.trim(),
            normalizedDecimal = normalizedDecimal,
            binary = finalBinary,
            octal = octal,
            hexadecimal = hexadecimal,
            naturalBitLength = naturalBitLength,
            analyzedBitLength = analyzedBitLength,
            oneBitCount = oneBitCount,
            zeroBitCount = zeroBitCount,
            highestSetBit = highestSetBit,
            lowestSetBit = lowestSetBit,
            setBitPositions = setBitPositions,
            clearBitPositions = clearBitPositions,
            bitDensityPercent = bitDensityPercent,
            bitWidthMode = bitWidthMode,
            calculatedAt = System.currentTimeMillis(),
            isNegative = isNegative,
            magnitudeBinary = paddedMagnitude
        )

        return Result.success(result)
    }

    /**
     * Check individual bit at given zero-based index.
     * Returns 1 if set, 0 if clear.
     */
    fun checkBit(
        position: Int,
        magnitude: BigInteger,
        analyzedBitLength: Int
    ): Result<Int> {
        if (position < 0 || position >= analyzedBitLength) {
            return Result.failure(
                IllegalArgumentException("Bit position is outside the current binary length.")
            )
        }
        val isSet = magnitude.testBit(position)
        return Result.success(if (isSet) 1 else 0)
    }
}
