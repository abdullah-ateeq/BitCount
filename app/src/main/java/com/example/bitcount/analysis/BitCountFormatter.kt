package com.example.bitcount.analysis

import com.example.bitcount.model.BitCountResult
import com.example.bitcount.model.BinaryGrouping
import java.math.BigDecimal
import java.math.BigInteger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BitCountFormatter {

    fun formatBinary(
        binaryString: String,
        grouping: BinaryGrouping
    ): String {
        if (grouping == BinaryGrouping.OFF || grouping.groupSize <= 0) {
            return binaryString
        }

        val isNegative = binaryString.startsWith('-')
        val rawDigits = if (isNegative) binaryString.substring(1) else binaryString

        if (rawDigits.isEmpty()) return binaryString

        val groupSize = grouping.groupSize
        val remainder = rawDigits.length % groupSize
        val sb = StringBuilder()

        var startIndex = 0
        if (remainder > 0) {
            sb.append(rawDigits.substring(0, remainder))
            startIndex = remainder
        }

        while (startIndex < rawDigits.length) {
            if (sb.isNotEmpty()) {
                sb.append(' ')
            }
            sb.append(rawDigits.substring(startIndex, startIndex + groupSize))
            startIndex += groupSize
        }

        return if (isNegative) "-$sb" else sb.toString()
    }

    fun formatDensity(density: BigDecimal): String {
        val stripped = density.stripTrailingZeros()
        val plain = stripped.toPlainString()
        return "$plain%"
    }

    fun formatBinaryCopy(binary: String, includePrefix: Boolean): String {
        return if (includePrefix) {
            if (binary.startsWith('-')) {
                "-0b${binary.substring(1)}"
            } else {
                "0b$binary"
            }
        } else {
            binary
        }
    }

    fun formatCopyAll(result: BitCountResult): String {
        val highestBitStr = result.highestSetBit?.let { "Bit $it" } ?: "None"
        val lowestBitStr = result.lowestSetBit?.let { "Bit $it" } ?: "None"
        val densityStr = formatDensity(result.bitDensityPercent)

        return buildString {
            appendLine("BitCount")
            appendLine()
            appendLine("Input:")
            appendLine(result.originalInput)
            appendLine()
            appendLine("Binary:")
            appendLine(result.binary)
            appendLine()
            appendLine("Binary Length:")
            appendLine("${result.analyzedBitLength} bits")
            appendLine()
            appendLine("1-Bits:")
            appendLine(result.oneBitCount)
            appendLine()
            appendLine("0-Bits:")
            appendLine(result.zeroBitCount)
            appendLine()
            appendLine("Highest Set Bit:")
            appendLine(highestBitStr)
            appendLine()
            appendLine("Lowest Set Bit:")
            appendLine(lowestBitStr)
            appendLine()
            appendLine("Bit Density:")
            appendLine(densityStr)
            appendLine()
            appendLine("Decimal:")
            appendLine(result.normalizedDecimal)
            appendLine()
            appendLine("Octal:")
            appendLine(result.octal)
            appendLine()
            appendLine("Hexadecimal:")
            append(result.hexadecimal)
        }
    }

    fun formatShare(result: BitCountResult): String {
        return formatCopyAll(result)
    }

    fun formatHistoryTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diffDays = (now - timestamp) / (24 * 60 * 60 * 1000)

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val formattedTime = timeFormat.format(Date(timestamp))

        return if (diffDays == 0L) {
            "Today • $formattedTime"
        } else if (diffDays == 1L) {
            "Yesterday • $formattedTime"
        } else {
            val dateFormat = SimpleDateFormat("MMM d • h:mm a", Locale.getDefault())
            dateFormat.format(Date(timestamp))
        }
    }

    /**
     * Breakdown showing powers of two for set bits.
     * Only practical for <= 32 set bits and bit index <= 128.
     */
    fun generateBitValueBreakdown(
        setBitPositions: List<Int>,
        normalizedDecimal: String
    ): List<String>? {
        if (setBitPositions.isEmpty()) return null
        if (setBitPositions.size > 32) return null
        if ((setBitPositions.firstOrNull() ?: 0) > 128) return null

        val lines = mutableListOf<String>()
        val values = mutableListOf<BigInteger>()

        for (pos in setBitPositions) {
            val powerVal = BigInteger.valueOf(2).pow(pos)
            values.add(powerVal)
            lines.add("Bit $pos = $powerVal")
        }

        val sumStr = values.joinToString(" + ") + " = " + values.fold(BigInteger.ZERO) { acc, v -> acc.add(v) }
        lines.add(sumStr)

        return lines
    }

    /**
     * Returns correlation of 4-bit chunks with their hex digit.
     */
    fun getHexCorrelation(magnitudeBinary: String): List<Pair<String, String>> {
        val padLength = if (magnitudeBinary.length % 4 == 0) 0 else 4 - (magnitudeBinary.length % 4)
        val padded = "0".repeat(padLength) + magnitudeBinary
        val chunks = padded.chunked(4)

        return chunks.map { chunk ->
            val hexVal = chunk.toInt(2).toString(16).uppercase()
            chunk to hexVal
        }
    }
}
