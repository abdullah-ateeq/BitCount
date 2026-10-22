package com.example.bitcount.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.bitcount.model.AppSettings
import com.example.bitcount.model.AppTheme
import com.example.bitcount.model.BinaryGrouping
import com.example.bitcount.model.BitWidthMode
import com.example.bitcount.model.InputBase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(
    context: Context? = null,
    sharedPreferences: SharedPreferences? = null
) {

    private val prefs: SharedPreferences = sharedPreferences
        ?: context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        ?: throw IllegalArgumentException("Either context or sharedPreferences must be provided")

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<AppSettings> = _settingsFlow.asStateFlow()

    private fun loadSettings(): AppSettings {
        val themeStr = prefs.getString(KEY_THEME, AppTheme.SYSTEM.name) ?: AppTheme.SYSTEM.name
        val theme = try {
            AppTheme.valueOf(themeStr)
        } catch (e: Exception) {
            AppTheme.SYSTEM
        }

        val dynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, true)
        val hapticFeedback = prefs.getBoolean(KEY_HAPTIC_FEEDBACK, true)
        val animations = prefs.getBoolean(KEY_ANIMATIONS, true)
        val confirmDelete = prefs.getBoolean(KEY_CONFIRM_DELETE, true)

        val groupingStr = prefs.getString(KEY_GROUPING, BinaryGrouping.FOUR.name) ?: BinaryGrouping.FOUR.name
        val grouping = try {
            BinaryGrouping.valueOf(groupingStr)
        } catch (e: Exception) {
            BinaryGrouping.FOUR
        }

        val showBitPositions = prefs.getBoolean(KEY_SHOW_BIT_POSITIONS, true)
        val binaryPrefixInCopy = prefs.getBoolean(KEY_PREFIX_IN_COPY, false)

        return AppSettings(
            theme = theme,
            dynamicColor = dynamicColor,
            hapticFeedback = hapticFeedback,
            animations = animations,
            confirmDelete = confirmDelete,
            groupBinaryBits = grouping,
            showBitPositions = showBitPositions,
            binaryPrefixInCopy = binaryPrefixInCopy
        )
    }

    fun updateTheme(theme: AppTheme) {
        prefs.edit().putString(KEY_THEME, theme.name).apply()
        _settingsFlow.value = _settingsFlow.value.copy(theme = theme)
    }

    fun updateDynamicColor(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(dynamicColor = enabled)
    }

    fun updateHapticFeedback(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC_FEEDBACK, enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(hapticFeedback = enabled)
    }

    fun updateAnimations(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ANIMATIONS, enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(animations = enabled)
    }

    fun updateConfirmDelete(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CONFIRM_DELETE, enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(confirmDelete = enabled)
    }

    fun updateGrouping(grouping: BinaryGrouping) {
        prefs.edit().putString(KEY_GROUPING, grouping.name).apply()
        _settingsFlow.value = _settingsFlow.value.copy(groupBinaryBits = grouping)
    }

    fun updateShowBitPositions(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_BIT_POSITIONS, show).apply()
        _settingsFlow.value = _settingsFlow.value.copy(showBitPositions = show)
    }

    fun updateBinaryPrefixInCopy(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PREFIX_IN_COPY, enabled).apply()
        _settingsFlow.value = _settingsFlow.value.copy(binaryPrefixInCopy = enabled)
    }

    fun resetSettings() {
        prefs.edit()
            .remove(KEY_THEME)
            .remove(KEY_DYNAMIC_COLOR)
            .remove(KEY_HAPTIC_FEEDBACK)
            .remove(KEY_ANIMATIONS)
            .remove(KEY_CONFIRM_DELETE)
            .remove(KEY_GROUPING)
            .remove(KEY_SHOW_BIT_POSITIONS)
            .remove(KEY_PREFIX_IN_COPY)
            .apply()

        _settingsFlow.value = AppSettings()
    }

    // Input state persistence
    fun saveLastInputState(input: String, base: InputBase, bitWidth: BitWidthMode) {
        val editor = prefs.edit()
            .putString(KEY_LAST_INPUT, input)
            .putString(KEY_LAST_BASE, base.name)

        when (bitWidth) {
            is BitWidthMode.Natural -> {
                editor.putString(KEY_LAST_WIDTH_TYPE, "NATURAL")
            }
            is BitWidthMode.Fixed -> {
                editor.putString(KEY_LAST_WIDTH_TYPE, "FIXED")
                editor.putInt(KEY_LAST_WIDTH_VALUE, bitWidth.width)
            }
        }
        editor.apply()
    }

    fun getLastInput(): String = prefs.getString(KEY_LAST_INPUT, "") ?: ""

    fun getLastBase(): InputBase {
        val name = prefs.getString(KEY_LAST_BASE, InputBase.DECIMAL.name) ?: InputBase.DECIMAL.name
        return try {
            InputBase.valueOf(name)
        } catch (e: Exception) {
            InputBase.DECIMAL
        }
    }

    fun getLastBitWidth(): BitWidthMode {
        val type = prefs.getString(KEY_LAST_WIDTH_TYPE, "NATURAL")
        return if (type == "FIXED") {
            val width = prefs.getInt(KEY_LAST_WIDTH_VALUE, 8)
            BitWidthMode.Fixed(width)
        } else {
            BitWidthMode.Natural
        }
    }

    companion object {
        private const val PREFS_NAME = "bitcount_settings_prefs"
        private const val KEY_THEME = "app_theme"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
        private const val KEY_HAPTIC_FEEDBACK = "haptic_feedback"
        private const val KEY_ANIMATIONS = "animations"
        private const val KEY_CONFIRM_DELETE = "confirm_delete"
        private const val KEY_GROUPING = "binary_grouping"
        private const val KEY_SHOW_BIT_POSITIONS = "show_bit_positions"
        private const val KEY_PREFIX_IN_COPY = "binary_prefix_in_copy"

        private const val KEY_LAST_INPUT = "last_input"
        private const val KEY_LAST_BASE = "last_base"
        private const val KEY_LAST_WIDTH_TYPE = "last_width_type"
        private const val KEY_LAST_WIDTH_VALUE = "last_width_value"
    }
}
