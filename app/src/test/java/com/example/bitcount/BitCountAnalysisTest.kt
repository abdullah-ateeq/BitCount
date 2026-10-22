package com.example.bitcount

import com.example.bitcount.analysis.BitCountAnalyzer
import com.example.bitcount.model.BitWidthMode
import com.example.bitcount.model.InputBase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.BigInteger

class BitCountAnalysisTest {

    private val analyzer = BitCountAnalyzer()

    // CRITICAL TEST — 255
    @Test
    fun testCritical255() {
        val result = analyzer.analyze("255", InputBase.DECIMAL, BitWidthMode.Natural).getOrThrow()
        assertEquals("11111111", result.binary)
        assertEquals(8, result.analyzedBitLength)
        assertEquals(8, result.naturalBitLength)
        assertEquals(8, result.oneBitCount)
        assertEquals(0, result.zeroBitCount)
        assertEquals(7, result.highestSetBit)
        assertEquals(0, result.lowestSetBit)
        assertEquals(BigDecimal("100"), result.bitDensityPercent)
        assertEquals("377", result.octal)
        assertEquals("FF", result.hexadecimal)
        assertEquals("255", result.normalizedDecimal)
        assertEquals(listOf(7, 6, 5, 4, 3, 2, 1, 0), result.setBitPositions)
        assertTrue(result.clearBitPositions.isEmpty())
    }

    // CRITICAL TEST — 10
    @Test
    fun testCritical10() {
        val result = analyzer.analyze("10", InputBase.DECIMAL, BitWidthMode.Natural).getOrThrow()
        assertEquals("1010", result.binary)
        assertEquals(4, result.analyzedBitLength)
        assertEquals(2, result.oneBitCount)
        assertEquals(2, result.zeroBitCount)
        assertEquals(3, result.highestSetBit)
        assertEquals(1, result.lowestSetBit)
        assertEquals(BigDecimal("50"), result.bitDensityPercent)
        assertEquals("12", result.octal)
        assertEquals("A", result.hexadecimal)
    }

    // CRITICAL TEST — 45
    @Test
    fun testCritical45() {
        val result = analyzer.analyze("45", InputBase.DECIMAL, BitWidthMode.Natural).getOrThrow()
        assertEquals("101101", result.binary)
        assertEquals(6, result.analyzedBitLength)
        assertEquals(4, result.oneBitCount)
        assertEquals(2, result.zeroBitCount)
        assertEquals(5, result.highestSetBit)
        assertEquals(0, result.lowestSetBit)
        assertEquals(listOf(5, 3, 2, 0), result.setBitPositions)
        assertEquals(listOf(4, 1), result.clearBitPositions)
        assertEquals(BigDecimal("66.6667"), result.bitDensityPercent)
    }

    // CRITICAL TEST — ZERO
    @Test
    fun testCriticalZero() {
        val result = analyzer.analyze("0", InputBase.DECIMAL, BitWidthMode.Natural).getOrThrow()
        assertEquals("0", result.binary)
        assertEquals(1, result.analyzedBitLength)
        assertEquals(1, result.naturalBitLength)
        assertEquals(0, result.oneBitCount)
        assertEquals(1, result.zeroBitCount)
        assertNull(result.highestSetBit)
        assertNull(result.lowestSetBit)
        assertEquals(BigDecimal.ZERO, result.bitDensityPercent)
        assertEquals("0", result.octal)
        assertEquals("0", result.hexadecimal)
        assertEquals("0", result.normalizedDecimal)
        assertTrue(result.setBitPositions.isEmpty())
        assertEquals(listOf(0), result.clearBitPositions)
    }

    // CRITICAL TEST — ONE
    @Test
    fun testCriticalOne() {
        val result = analyzer.analyze("1", InputBase.DECIMAL, BitWidthMode.Natural).getOrThrow()
        assertEquals("1", result.binary)
        assertEquals(1, result.analyzedBitLength)
        assertEquals(1, result.oneBitCount)
        assertEquals(0, result.zeroBitCount)
        assertEquals(0, result.highestSetBit)
        assertEquals(0, result.lowestSetBit)
        assertEquals(BigDecimal("100"), result.bitDensityPercent)
    }

    // CRITICAL TEST — POWER OF TWO (256)
    @Test
    fun testCritical256() {
        val result = analyzer.analyze("256", InputBase.DECIMAL, BitWidthMode.Natural).getOrThrow()
        assertEquals("100000000", result.binary)
        assertEquals(9, result.analyzedBitLength)
        assertEquals(1, result.oneBitCount)
        assertEquals(8, result.zeroBitCount)
        assertEquals(8, result.highestSetBit)
        assertEquals(8, result.lowestSetBit)
        assertEquals(BigDecimal("11.1111"), result.bitDensityPercent)
    }

    // CRITICAL TEST — NEGATIVE VALUE (-10)
    @Test
    fun testCriticalNegativeValue() {
        val result = analyzer.analyze("-10", InputBase.DECIMAL, BitWidthMode.Natural).getOrThrow()
        assertEquals("-1010", result.binary)
        assertTrue(result.isNegative)
        assertEquals("1010", result.magnitudeBinary)
        assertEquals(4, result.analyzedBitLength)
        assertEquals(2, result.oneBitCount)
        assertEquals(2, result.zeroBitCount)
        assertEquals(3, result.highestSetBit)
        assertEquals(1, result.lowestSetBit)
        assertEquals("-10", result.normalizedDecimal)
        assertEquals("-12", result.octal)
        assertEquals("-A", result.hexadecimal)
    }

    // CRITICAL TEST — BINARY INPUT (101101)
    @Test
    fun testCriticalBinaryInput() {
        val result = analyzer.analyze("101101", InputBase.BINARY, BitWidthMode.Natural).getOrThrow()
        assertEquals("45", result.normalizedDecimal)
        assertEquals("101101", result.binary)
        assertEquals(6, result.analyzedBitLength)
        assertEquals(4, result.oneBitCount)
        assertEquals(2, result.zeroBitCount)
    }

    // CRITICAL TEST — LEADING ZERO BINARY (00001010)
    @Test
    fun testCriticalLeadingZeroBinary() {
        // Natural mode
        val natural = analyzer.analyze("00001010", InputBase.BINARY, BitWidthMode.Natural).getOrThrow()
        assertEquals("1010", natural.binary)
        assertEquals(4, natural.naturalBitLength)
        assertEquals(4, natural.analyzedBitLength)

        // 8-bit mode
        val fixed8 = analyzer.analyze("00001010", InputBase.BINARY, BitWidthMode.Fixed(8)).getOrThrow()
        assertEquals("00001010", fixed8.binary)
        assertEquals(8, fixed8.analyzedBitLength)
        assertEquals(4, fixed8.naturalBitLength)
        assertEquals(2, fixed8.oneBitCount)
        assertEquals(6, fixed8.zeroBitCount)
    }

    // CRITICAL TEST — FIXED WIDTH (10, 8-bit)
    @Test
    fun testCriticalFixedWidth() {
        val result = analyzer.analyze("10", InputBase.DECIMAL, BitWidthMode.Fixed(8)).getOrThrow()
        assertEquals("00001010", result.binary)
        assertEquals(8, result.analyzedBitLength)
        assertEquals(2, result.oneBitCount)
        assertEquals(6, result.zeroBitCount)
        assertEquals(4, result.naturalBitLength)
        assertEquals(listOf(3, 1), result.setBitPositions)
        assertEquals(listOf(7, 6, 5, 4, 2, 0), result.clearBitPositions)
    }

    // CRITICAL TEST — WIDTH TOO SMALL (256 in 8-bit)
    @Test
    fun testCriticalWidthTooSmall() {
        val result = analyzer.analyze("256", InputBase.DECIMAL, BitWidthMode.Fixed(8))
        assertTrue(result.isFailure)
        assertEquals(
            "This value does not fit within 8 bits. Choose a larger width.",
            result.exceptionOrNull()?.message
        )
    }

    // FIXED WIDTH AND ZERO
    @Test
    fun testFixedWidthAndZero() {
        val result8 = analyzer.analyze("0", InputBase.DECIMAL, BitWidthMode.Fixed(8)).getOrThrow()
        assertEquals("00000000", result8.binary)
        assertEquals(8, result8.analyzedBitLength)
        assertEquals(0, result8.oneBitCount)
        assertEquals(8, result8.zeroBitCount)

        val result16 = analyzer.analyze("0", InputBase.DECIMAL, BitWidthMode.Fixed(16)).getOrThrow()
        assertEquals("0".repeat(16), result16.binary)
        assertEquals(16, result16.analyzedBitLength)
        assertEquals(0, result16.oneBitCount)
        assertEquals(16, result16.zeroBitCount)
    }

    // NEGATIVE FIXED WIDTH
    @Test
    fun testNegativeFixedWidth() {
        val result = analyzer.analyze("-10", InputBase.DECIMAL, BitWidthMode.Fixed(8)).getOrThrow()
        assertEquals("-00001010", result.binary)
        assertEquals("00001010", result.magnitudeBinary)
        assertTrue(result.isNegative)
        assertEquals(8, result.analyzedBitLength)
        assertEquals(2, result.oneBitCount)
        assertEquals(6, result.zeroBitCount)
    }

    // CRITICAL TEST — LARGE INTEGER (18446744073709551616)
    @Test
    fun testCriticalLargeInteger() {
        val result = analyzer.analyze("18446744073709551616", InputBase.DECIMAL, BitWidthMode.Natural).getOrThrow()
        assertEquals(65, result.analyzedBitLength)
        assertEquals(65, result.naturalBitLength)
        assertEquals(1, result.oneBitCount)
        assertEquals(64, result.zeroBitCount)
        assertEquals("1" + "0".repeat(64), result.binary)
        assertEquals(64, result.highestSetBit)
        assertEquals(64, result.lowestSetBit)
    }

    // CRITICAL TEST — LARGE BINARY (50 ones)
    @Test
    fun testCriticalLargeBinary() {
        val binaryStr = "1".repeat(50)
        val result = analyzer.analyze(binaryStr, InputBase.BINARY, BitWidthMode.Natural).getOrThrow()
        assertEquals(50, result.analyzedBitLength)
        assertEquals(50, result.oneBitCount)
        assertEquals(0, result.zeroBitCount)
        assertEquals(49, result.highestSetBit)
        assertEquals(0, result.lowestSetBit)
        assertEquals(binaryStr, result.binary)
        assertEquals(BigInteger(binaryStr, 2).toString(10), result.normalizedDecimal)
        assertEquals(BigInteger(binaryStr, 2).toString(16).uppercase(), result.hexadecimal)
        assertEquals(BigInteger(binaryStr, 2).toString(8), result.octal)
    }

    // 20. Bit Density Calculation test
    @Test
    fun testBitDensityCalculation() {
        val result = analyzer.analyze("10000000", InputBase.BINARY, BitWidthMode.Natural).getOrThrow()
        assertEquals(8, result.analyzedBitLength)
        assertEquals(1, result.oneBitCount)
        assertEquals(BigDecimal("12.5"), result.bitDensityPercent)
    }

    // 27. Individual bit lookup
    @Test
    fun testIndividualBitLookup() {
        val magnitude45 = BigInteger("45") // 101101 (bits 5,3,2,0 set)
        val bit0 = analyzer.checkBit(0, magnitude45, 6).getOrThrow()
        assertEquals(1, bit0)

        val bit1 = analyzer.checkBit(1, magnitude45, 6).getOrThrow()
        assertEquals(0, bit1)

        val bit2 = analyzer.checkBit(2, magnitude45, 6).getOrThrow()
        assertEquals(1, bit2)

        val bit3 = analyzer.checkBit(3, magnitude45, 6).getOrThrow()
        assertEquals(1, bit3)

        val bit4 = analyzer.checkBit(4, magnitude45, 6).getOrThrow()
        assertEquals(0, bit4)

        val bit5 = analyzer.checkBit(5, magnitude45, 6).getOrThrow()
        assertEquals(1, bit5)

        val bit6Out = analyzer.checkBit(6, magnitude45, 6)
        assertTrue(bit6Out.isFailure)
        assertEquals("Bit position is outside the current binary length.", bit6Out.exceptionOrNull()?.message)
    }
}
