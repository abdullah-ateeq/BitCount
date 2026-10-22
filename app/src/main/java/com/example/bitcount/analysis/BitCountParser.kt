package com.example.bitcount.analysis

import com.example.bitcount.model.InputBase
import java.math.BigInteger

class BitCountParser {

    fun parse(input: String, inputBase: InputBase): Result<BigInteger> {
        val validation = BitCountValidator.validateInput(input, inputBase)
        if (validation.isFailure) {
            return Result.failure(validation.exceptionOrNull() ?: IllegalArgumentException("Invalid input"))
        }

        var trimmed = input.trim()
        val isNegative = trimmed.startsWith('-')
        if (isNegative || trimmed.startsWith('+')) {
            trimmed = trimmed.substring(1)
        }

        return try {
            when (inputBase) {
                InputBase.DECIMAL -> {
                    val parsed = BigInteger(trimmed, 10)
                    // Normalize -0 to 0
                    val finalValue = if (isNegative && parsed != BigInteger.ZERO) parsed.negate() else parsed
                    Result.success(finalValue)
                }
                InputBase.BINARY -> {
                    var binaryDigits = trimmed
                    if (binaryDigits.startsWith("0b", ignoreCase = true)) {
                        binaryDigits = binaryDigits.substring(2)
                    }
                    val parsed = BigInteger(binaryDigits, 2)
                    // Normalize -0 to 0
                    val finalValue = if (isNegative && parsed != BigInteger.ZERO) parsed.negate() else parsed
                    Result.success(finalValue)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
