package com.example.bitcount

import com.example.bitcount.analysis.BitCountAnalyzer
import com.example.bitcount.analysis.BitCountFormatter
import com.example.bitcount.model.BinaryGrouping
import com.example.bitcount.model.BitWidthMode
import com.example.bitcount.model.InputBase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class BitCountFormattingTest {

    private val analyzer = BitCountAnalyzer()

    // 29. Group-of-4 formatting
    @Test
    fun testGroupOfFourFormatting() {
        val grouped1 = BitCountFormatter.formatBinary("1111111111111111", BinaryGrouping.FOUR)
        assertEquals("1111 1111 1111 1111", grouped1)

        val grouped2 = BitCountFormatter.formatBinary("101101", BinaryGrouping.FOUR)
        assertEquals("10 1101", grouped2)

        val groupedNegative = BitCountFormatter.formatBinary("-101101", BinaryGrouping.FOUR)
        assertEquals("-10 1101", groupedNegative)

        val off = BitCountFormatter.formatBinary("11111111", BinaryGrouping.OFF)
        assertEquals("11111111", off)
    }

    // 30. Group-of-8 formatting
    @Test
    fun testGroupOfEightFormatting() {
        val grouped1 = BitCountFormatter.formatBinary("1111111100000000", BinaryGrouping.EIGHT)
        assertEquals("11111111 00000000", grouped1)

        val grouped2 = BitCountFormatter.formatBinary("1101101", BinaryGrouping.EIGHT)
        assertEquals("1101101", grouped2)
    }

    // 38. Copy formatting
    @Test
    fun testCopyFormatting() {
        // Plain binary copy without prefix
        val plain = BitCountFormatter.formatBinaryCopy("11111111", false)
        assertEquals("11111111", plain)

        // Binary copy with prefix
        val prefixed = BitCountFormatter.formatBinaryCopy("11111111", true)
        assertEquals("0b11111111", prefixed)

        // Negative copy with prefix
        val negativePrefixed = BitCountFormatter.formatBinaryCopy("-1010", true)
        assertEquals("-0b1010", negativePrefixed)

        // Copy All Report for 255
        val result255 = analyzer.analyze("255", InputBase.DECIMAL, BitWidthMode.Natural).getOrThrow()
        val copyAllText = BitCountFormatter.formatCopyAll(result255)

        assertTrue(copyAllText.contains("BitCount"))
        assertTrue(copyAllText.contains("Input:\n255"))
        assertTrue(copyAllText.contains("Binary:\n11111111"))
        assertTrue(copyAllText.contains("Binary Length:\n8 bits"))
        assertTrue(copyAllText.contains("1-Bits:\n8"))
        assertTrue(copyAllText.contains("0-Bits:\n0"))
        assertTrue(copyAllText.contains("Highest Set Bit:\nBit 7"))
        assertTrue(copyAllText.contains("Lowest Set Bit:\nBit 0"))
        assertTrue(copyAllText.contains("Bit Density:\n100%"))
        assertTrue(copyAllText.contains("Decimal:\n255"))
        assertTrue(copyAllText.contains("Octal:\n377"))
        assertTrue(copyAllText.contains("Hexadecimal:\nFF"))
    }

    // 39. Share formatting
    @Test
    fun testShareFormatting() {
        val result = analyzer.analyze("10", InputBase.DECIMAL, BitWidthMode.Natural).getOrThrow()
        val shareText = BitCountFormatter.formatShare(result)

        assertTrue(shareText.contains("Input:\n10"))
        assertTrue(shareText.contains("Binary:\n1010"))
        assertTrue(shareText.contains("1-Bits:\n2"))
        assertTrue(shareText.contains("0-Bits:\n2"))
        assertTrue(shareText.contains("Bit Density:\n50%"))
    }

    // Density formatting
    @Test
    fun testDensityFormatting() {
        assertEquals("100%", BitCountFormatter.formatDensity(BigDecimal("100")))
        assertEquals("50%", BitCountFormatter.formatDensity(BigDecimal("50")))
        assertEquals("12.5%", BitCountFormatter.formatDensity(BigDecimal("12.5")))
        assertEquals("66.6667%", BitCountFormatter.formatDensity(BigDecimal("66.6667")))
    }

    // Bit value breakdown
    @Test
    fun testBitValueBreakdown() {
        val result = analyzer.analyze("45", InputBase.DECIMAL, BitWidthMode.Natural).getOrThrow()
        val breakdown = BitCountFormatter.generateBitValueBreakdown(result.setBitPositions, result.normalizedDecimal)

        assertNotNull(breakdown)
        assertTrue(breakdown!!.contains("Bit 5 = 32"))
        assertTrue(breakdown.contains("Bit 3 = 8"))
        assertTrue(breakdown.contains("Bit 2 = 4"))
        assertTrue(breakdown.contains("Bit 0 = 1"))
        assertTrue(breakdown.last().contains("32 + 8 + 4 + 1 = 45"))
    }

    // Hex correlation
    @Test
    fun testHexCorrelation() {
        val pairs = BitCountFormatter.getHexCorrelation("11111111")
        assertEquals(2, pairs.size)
        assertEquals("1111" to "F", pairs[0])
        assertEquals("1111" to "F", pairs[1])

        val pairs2 = BitCountFormatter.getHexCorrelation("1010")
        assertEquals(1, pairs2.size)
        assertEquals("1010" to "A", pairs2[0])
    }
}
