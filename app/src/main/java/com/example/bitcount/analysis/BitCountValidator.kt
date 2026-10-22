package com.example.bitcount.analysis

import com.example.bitcount.model.InputBase

object BitCountValidator {

    const val MAX_INPUT_LENGTH = 2000
    const val MIN_CUSTOM_WIDTH = 1
    const val MAX_CUSTOM_WIDTH = 2000

    fun validateInput(input: String, inputBase: InputBase): Result<String> {
        val trimmed = input.trim()

        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Enter a number to analyze."))
        }

        if (trimmed.length > MAX_INPUT_LENGTH) {
            return Result.failure(IllegalArgumentException("Number is too long. Maximum 2000 characters."))
        }

        // Check decimal fractions (e.g. 10.5, 10,5)
        if (trimmed.contains('.') || trimmed.contains(',')) {
            return Result.failure(IllegalArgumentException("Only whole numbers are supported."))
        }

        return when (inputBase) {
            InputBase.DECIMAL -> validateDecimal(trimmed)
            InputBase.BINARY -> validateBinary(trimmed)
        }
    }

    private fun validateDecimal(input: String): Result<String> {
        var digits = input
        if (digits.startsWith('+') || digits.startsWith('-')) {
            digits = digits.substring(1)
        }

        if (digits.isEmpty()) {
            return Result.failure(IllegalArgumentException("Invalid decimal number. Use digits 0–9."))
        }

        for (char in digits) {
            if (char !in '0'..'9') {
                return Result.failure(IllegalArgumentException("Invalid decimal number. Use digits 0–9."))
            }
        }

        return Result.success(input)
    }

    private fun validateBinary(input: String): Result<String> {
        var digits = input
        if (digits.startsWith('+') || digits.startsWith('-')) {
            digits = digits.substring(1)
        }

        if (digits.startsWith("0b", ignoreCase = true)) {
            digits = digits.substring(2)
        }

        if (digits.isEmpty()) {
            return Result.failure(IllegalArgumentException("Invalid binary number. Use only 0 and 1."))
        }

        for (char in digits) {
            if (char != '0' && char != '1') {
                return Result.failure(IllegalArgumentException("Invalid binary number. Use only 0 and 1."))
            }
        }

        return Result.success(input)
    }

    fun validateCustomWidth(input: String): Result<Int> {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Enter a bit width from 1 to 2000."))
        }
        val width = trimmed.toIntOrNull()
        if (width == null || width < MIN_CUSTOM_WIDTH || width > MAX_CUSTOM_WIDTH) {
            return Result.failure(IllegalArgumentException("Enter a bit width from 1 to 2000."))
        }
        return Result.success(width)
    }

    fun validateBitPosition(positionStr: String, analyzedBitLength: Int): Result<Int> {
        val trimmed = positionStr.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Enter a bit position."))
        }
        val position = trimmed.toIntOrNull()
        if (position == null || position < 0 || position >= analyzedBitLength) {
            return Result.failure(IllegalArgumentException("Bit position is outside the current binary length."))
        }
        return Result.success(position)
    }
}
