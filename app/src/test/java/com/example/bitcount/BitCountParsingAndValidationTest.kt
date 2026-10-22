package com.example.bitcount

import com.example.bitcount.analysis.BitCountParser
import com.example.bitcount.analysis.BitCountValidator
import com.example.bitcount.model.InputBase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigInteger

class BitCountParsingAndValidationTest {

    private val parser = BitCountParser()

    // 1. Decimal parsing
    @Test
    fun testDecimalParsing() {
        val result = parser.parse("255", InputBase.DECIMAL)
        assertTrue(result.isSuccess)
        assertEquals(BigInteger("255"), result.getOrNull())

        val resultLarge = parser.parse("18446744073709551616", InputBase.DECIMAL)
        assertTrue(resultLarge.isSuccess)
        assertEquals(BigInteger("18446744073709551616"), resultLarge.getOrNull())
    }

    // 2. Binary parsing
    @Test
    fun testBinaryParsing() {
        val result = parser.parse("101101", InputBase.BINARY)
        assertTrue(result.isSuccess)
        assertEquals(BigInteger("45"), result.getOrNull())

        val result255 = parser.parse("11111111", InputBase.BINARY)
        assertTrue(result255.isSuccess)
        assertEquals(BigInteger("255"), result255.getOrNull())
    }

    // 3. Binary prefix
    @Test
    fun testBinaryPrefix() {
        val result1 = parser.parse("0b1010", InputBase.BINARY)
        assertTrue(result1.isSuccess)
        assertEquals(BigInteger("10"), result1.getOrNull())

        val result2 = parser.parse("0B11111111", InputBase.BINARY)
        assertTrue(result2.isSuccess)
        assertEquals(BigInteger("255"), result2.getOrNull())
    }

    // 4. Positive sign
    @Test
    fun testPositiveSign() {
        val resDec = parser.parse("+255", InputBase.DECIMAL)
        assertTrue(resDec.isSuccess)
        assertEquals(BigInteger("255"), resDec.getOrNull())

        val resBin = parser.parse("+0b1010", InputBase.BINARY)
        assertTrue(resBin.isSuccess)
        assertEquals(BigInteger("10"), resBin.getOrNull())
    }

    // 5. Negative values
    @Test
    fun testNegativeValues() {
        val resDec = parser.parse("-10", InputBase.DECIMAL)
        assertTrue(resDec.isSuccess)
        assertEquals(BigInteger("-10"), resDec.getOrNull())

        val resBin = parser.parse("-1010", InputBase.BINARY)
        assertTrue(resBin.isSuccess)
        assertEquals(BigInteger("-10"), resBin.getOrNull())

        val resBinPrefix = parser.parse("-0b1010", InputBase.BINARY)
        assertTrue(resBinPrefix.isSuccess)
        assertEquals(BigInteger("-10"), resBinPrefix.getOrNull())
    }

    // 6. Negative zero
    @Test
    fun testNegativeZero() {
        val resDec = parser.parse("-0", InputBase.DECIMAL)
        assertTrue(resDec.isSuccess)
        assertEquals(BigInteger.ZERO, resDec.getOrNull())
        assertEquals(0, resDec.getOrNull()?.signum())

        val resBin = parser.parse("-0b0", InputBase.BINARY)
        assertTrue(resBin.isSuccess)
        assertEquals(BigInteger.ZERO, resBin.getOrNull())
    }

    // 7. Zero
    @Test
    fun testZero() {
        val resDec = parser.parse("0", InputBase.DECIMAL)
        assertTrue(resDec.isSuccess)
        assertEquals(BigInteger.ZERO, resDec.getOrNull())

        val resBin = parser.parse("0", InputBase.BINARY)
        assertTrue(resBin.isSuccess)
        assertEquals(BigInteger.ZERO, resBin.getOrNull())
    }

    // 8. One
    @Test
    fun testOne() {
        val resDec = parser.parse("1", InputBase.DECIMAL)
        assertTrue(resDec.isSuccess)
        assertEquals(BigInteger.ONE, resDec.getOrNull())

        val resBin = parser.parse("1", InputBase.BINARY)
        assertTrue(resBin.isSuccess)
        assertEquals(BigInteger.ONE, resBin.getOrNull())
    }

    // 33. Input length protection (2000 chars)
    @Test
    fun testInputLengthProtection() {
        val validLength = "1".repeat(2000)
        val validRes = BitCountValidator.validateInput(validLength, InputBase.BINARY)
        assertTrue(validRes.isSuccess)

        val invalidLength = "1".repeat(2001)
        val invalidRes = BitCountValidator.validateInput(invalidLength, InputBase.BINARY)
        assertTrue(invalidRes.isFailure)
        assertEquals("Number is too long. Maximum 2000 characters.", invalidRes.exceptionOrNull()?.message)
    }

    // 34. Invalid binary input
    @Test
    fun testInvalidBinaryInput() {
        val res1 = BitCountValidator.validateInput("10201", InputBase.BINARY)
        assertTrue(res1.isFailure)
        assertEquals("Invalid binary number. Use only 0 and 1.", res1.exceptionOrNull()?.message)

        val res2 = BitCountValidator.validateInput("abc", InputBase.BINARY)
        assertTrue(res2.isFailure)
        assertEquals("Invalid binary number. Use only 0 and 1.", res2.exceptionOrNull()?.message)

        val res3 = BitCountValidator.validateInput("0b", InputBase.BINARY)
        assertTrue(res3.isFailure)
        assertEquals("Invalid binary number. Use only 0 and 1.", res3.exceptionOrNull()?.message)
    }

    // 35. Invalid decimal input
    @Test
    fun testInvalidDecimalInput() {
        val res1 = BitCountValidator.validateInput("12A", InputBase.DECIMAL)
        assertTrue(res1.isFailure)
        assertEquals("Invalid decimal number. Use digits 0–9.", res1.exceptionOrNull()?.message)

        val res2 = BitCountValidator.validateInput("abc", InputBase.DECIMAL)
        assertTrue(res2.isFailure)
        assertEquals("Invalid decimal number. Use digits 0–9.", res2.exceptionOrNull()?.message)

        val res3 = BitCountValidator.validateInput("+", InputBase.DECIMAL)
        assertTrue(res3.isFailure)
        assertEquals("Invalid decimal number. Use digits 0–9.", res3.exceptionOrNull()?.message)
    }

    // 36. Decimal fractions
    @Test
    fun testDecimalFractions() {
        val res1 = BitCountValidator.validateInput("10.5", InputBase.DECIMAL)
        assertTrue(res1.isFailure)
        assertEquals("Only whole numbers are supported.", res1.exceptionOrNull()?.message)

        val res2 = BitCountValidator.validateInput("10,5", InputBase.DECIMAL)
        assertTrue(res2.isFailure)
        assertEquals("Only whole numbers are supported.", res2.exceptionOrNull()?.message)
    }

    // 37. Empty input
    @Test
    fun testEmptyInput() {
        val res1 = BitCountValidator.validateInput("", InputBase.DECIMAL)
        assertTrue(res1.isFailure)
        assertEquals("Enter a number to analyze.", res1.exceptionOrNull()?.message)

        val res2 = BitCountValidator.validateInput("   ", InputBase.BINARY)
        assertTrue(res2.isFailure)
        assertEquals("Enter a number to analyze.", res2.exceptionOrNull()?.message)
    }

    // 26. Custom width validation
    @Test
    fun testCustomWidthValidation() {
        val valid = BitCountValidator.validateCustomWidth("128")
        assertTrue(valid.isSuccess)
        assertEquals(128, valid.getOrNull())

        val zero = BitCountValidator.validateCustomWidth("0")
        assertTrue(zero.isFailure)
        assertEquals("Enter a bit width from 1 to 2000.", zero.exceptionOrNull()?.message)

        val negative = BitCountValidator.validateCustomWidth("-1")
        assertTrue(negative.isFailure)

        val tooLarge = BitCountValidator.validateCustomWidth("2001")
        assertTrue(tooLarge.isFailure)

        val invalidChars = BitCountValidator.validateCustomWidth("abc")
        assertTrue(invalidChars.isFailure)
    }

    // 28. Invalid bit position
    @Test
    fun testBitPositionValidation() {
        val valid = BitCountValidator.validateBitPosition("7", 8)
        assertTrue(valid.isSuccess)
        assertEquals(7, valid.getOrNull())

        val outside = BitCountValidator.validateBitPosition("8", 8)
        assertTrue(outside.isFailure)
        assertEquals("Bit position is outside the current binary length.", outside.exceptionOrNull()?.message)

        val negative = BitCountValidator.validateBitPosition("-1", 8)
        assertTrue(negative.isFailure)

        val empty = BitCountValidator.validateBitPosition("", 8)
        assertTrue(empty.isFailure)
    }
}
