package com.example.bitcount

import com.example.bitcount.analysis.BitCountAnalyzer
import com.example.bitcount.model.AppSettings
import com.example.bitcount.model.AppTheme
import com.example.bitcount.model.BinaryGrouping
import com.example.bitcount.model.BitWidthMode
import com.example.bitcount.model.InputBase
import com.example.bitcount.repository.HistoryRepository
import com.example.bitcount.repository.SettingsRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BitCountHistoryAndSettingsTest {

    private lateinit var fakeHistoryPrefs: FakeSharedPreferences
    private lateinit var fakeSettingsPrefs: FakeSharedPreferences
    private lateinit var historyRepository: HistoryRepository
    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setUp() {
        fakeHistoryPrefs = FakeSharedPreferences()
        fakeSettingsPrefs = FakeSharedPreferences()
        historyRepository = HistoryRepository(sharedPreferences = fakeHistoryPrefs)
        settingsRepository = SettingsRepository(sharedPreferences = fakeSettingsPrefs)
    }

    // 40. History save
    @Test
    fun testHistorySave() {
        val saved = historyRepository.addHistory(
            inputBase = InputBase.DECIMAL,
            input = "255",
            bitWidth = BitWidthMode.Natural
        )
        assertTrue(saved)

        val historyList = historyRepository.historyFlow.value
        assertEquals(1, historyList.size)
        assertEquals("255", historyList[0].input)
        assertEquals(InputBase.DECIMAL, historyList[0].inputBase)
        assertEquals(BitWidthMode.Natural, historyList[0].bitWidth)
    }

    // 41. History restore
    @Test
    fun testHistoryRestore() {
        historyRepository.addHistory(
            inputBase = InputBase.BINARY,
            input = "101101",
            bitWidth = BitWidthMode.Fixed(8)
        )

        val item = historyRepository.historyFlow.value.first()
        // Restore and analyze fresh
        val analyzer = BitCountAnalyzer()
        val restoredResult = analyzer.analyze(item.input, item.inputBase, item.bitWidth).getOrThrow()

        assertEquals("45", restoredResult.normalizedDecimal)
        assertEquals(8, restoredResult.analyzedBitLength)
        assertEquals("00101101", restoredResult.binary)
    }

    // 42. History limit (max 10 entries, FIFO)
    @Test
    fun testHistoryLimit() {
        // Add 12 distinct entries
        for (i in 1..12) {
            historyRepository.addHistory(
                inputBase = InputBase.DECIMAL,
                input = "$i",
                bitWidth = BitWidthMode.Natural
            )
        }

        val historyList = historyRepository.historyFlow.value
        assertEquals(10, historyList.size)
        // Most recent should be at the front
        assertEquals("12", historyList[0].input)
        assertEquals("11", historyList[1].input)
        // Oldest retained should be 3 (1 and 2 dropped)
        assertEquals("3", historyList.last().input)
    }

    // 43. Duplicate history (no consecutive duplicates)
    @Test
    fun testDuplicateHistory() {
        val firstSave = historyRepository.addHistory(
            inputBase = InputBase.DECIMAL,
            input = "255",
            bitWidth = BitWidthMode.Natural
        )
        assertTrue(firstSave)
        assertEquals(1, historyRepository.historyFlow.value.size)

        // Save exact same consecutively
        val secondSave = historyRepository.addHistory(
            inputBase = InputBase.DECIMAL,
            input = "255",
            bitWidth = BitWidthMode.Natural
        )
        assertFalse(secondSave)
        assertEquals(1, historyRepository.historyFlow.value.size)

        // Save different item, then 255 again (non-consecutive is allowed)
        historyRepository.addHistory(
            inputBase = InputBase.DECIMAL,
            input = "10",
            bitWidth = BitWidthMode.Natural
        )
        val thirdSave = historyRepository.addHistory(
            inputBase = InputBase.DECIMAL,
            input = "255",
            bitWidth = BitWidthMode.Natural
        )
        assertTrue(thirdSave)
        assertEquals(3, historyRepository.historyFlow.value.size)
    }

    // 44. History deletion
    @Test
    fun testHistoryDeletion() {
        historyRepository.addHistory(InputBase.DECIMAL, "10", BitWidthMode.Natural)
        historyRepository.addHistory(InputBase.DECIMAL, "20", BitWidthMode.Natural)

        val items = historyRepository.historyFlow.value
        assertEquals(2, items.size)

        val toDelete = items[0]
        historyRepository.deleteHistory(toDelete.id)

        val updated = historyRepository.historyFlow.value
        assertEquals(1, updated.size)
        assertEquals("10", updated[0].input)

        // Clear all
        historyRepository.clearAllHistory()
        assertTrue(historyRepository.historyFlow.value.isEmpty())
    }

    // 45. Settings persistence
    @Test
    fun testSettingsPersistence() {
        settingsRepository.updateTheme(AppTheme.DARK)
        settingsRepository.updateGrouping(BinaryGrouping.EIGHT)
        settingsRepository.updateShowBitPositions(false)
        settingsRepository.updateDynamicColor(false)
        settingsRepository.updateHapticFeedback(false)
        settingsRepository.updateAnimations(false)
        settingsRepository.updateConfirmDelete(false)
        settingsRepository.updateBinaryPrefixInCopy(true)

        // Instantiate new repository from same shared preferences to test persistence
        val reloaded = SettingsRepository(sharedPreferences = fakeSettingsPrefs)
        val settings = reloaded.settingsFlow.value

        assertEquals(AppTheme.DARK, settings.theme)
        assertEquals(BinaryGrouping.EIGHT, settings.groupBinaryBits)
        assertFalse(settings.showBitPositions)
        assertFalse(settings.dynamicColor)
        assertFalse(settings.hapticFeedback)
        assertFalse(settings.animations)
        assertFalse(settings.confirmDelete)
        assertTrue(settings.binaryPrefixInCopy)
    }

    // 46. Settings reset
    @Test
    fun testSettingsReset() {
        // Change settings
        settingsRepository.updateTheme(AppTheme.DARK)
        settingsRepository.updateGrouping(BinaryGrouping.OFF)
        settingsRepository.updateShowBitPositions(false)

        // Save a history entry
        historyRepository.addHistory(InputBase.DECIMAL, "45", BitWidthMode.Natural)

        // Reset settings
        settingsRepository.resetSettings()

        val reset = settingsRepository.settingsFlow.value
        assertEquals(AppTheme.SYSTEM, reset.theme)
        assertEquals(BinaryGrouping.FOUR, reset.groupBinaryBits)
        assertTrue(reset.showBitPositions)
        assertTrue(reset.dynamicColor)
        assertTrue(reset.hapticFeedback)
        assertTrue(reset.animations)
        assertTrue(reset.confirmDelete)
        assertFalse(reset.binaryPrefixInCopy)

        // History remains intact
        assertEquals(1, historyRepository.historyFlow.value.size)
        assertEquals("45", historyRepository.historyFlow.value[0].input)
    }
}
