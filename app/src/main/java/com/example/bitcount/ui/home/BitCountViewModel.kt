package com.example.bitcount.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitcount.analysis.BitCountAnalyzer
import com.example.bitcount.analysis.BitCountValidator
import com.example.bitcount.model.AppSettings
import com.example.bitcount.model.AppTheme
import com.example.bitcount.model.BitCountHistory
import com.example.bitcount.model.BinaryGrouping
import com.example.bitcount.model.BitWidthMode
import com.example.bitcount.model.InputBase
import com.example.bitcount.repository.HistoryRepository
import com.example.bitcount.repository.SettingsRepository
import com.example.bitcount.util.HapticHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigInteger

class BitCountViewModel @JvmOverloads constructor(
    application: Application,
    private val analyzer: BitCountAnalyzer = BitCountAnalyzer(),
    private val historyRepository: HistoryRepository = HistoryRepository(application),
    private val settingsRepository: SettingsRepository = SettingsRepository(application),
    private val hapticHelper: HapticHelper = HapticHelper(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(
        BitCountUiState(
            input = settingsRepository.getLastInput(),
            inputBase = settingsRepository.getLastBase(),
            bitWidthMode = settingsRepository.getLastBitWidth()
        )
    )
    val uiState: StateFlow<BitCountUiState> = _uiState.asStateFlow()

    init {
        // Collect settings
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }

        // Collect history
        viewModelScope.launch {
            historyRepository.historyFlow.collect { history ->
                _uiState.update { it.copy(history = history) }
            }
        }

        // Initial analysis if last input was saved
        if (_uiState.value.input.isNotBlank()) {
            analyze()
        }
    }

    fun onInputChange(newInput: String) {
        _uiState.update {
            it.copy(
                input = newInput,
                errorMessage = null
            )
        }
    }

    fun onInputBaseChange(newBase: InputBase) {
        if (_uiState.value.inputBase != newBase) {
            hapticHelper.performLightClick(_uiState.value.settings.hapticFeedback)
            _uiState.update {
                it.copy(
                    inputBase = newBase,
                    errorMessage = null,
                    result = null,
                    bitPositionInput = "",
                    bitPositionResult = null,
                    bitPositionError = null
                )
            }
        }
    }

    fun onBitWidthModeChange(mode: BitWidthMode) {
        hapticHelper.performTick(_uiState.value.settings.hapticFeedback)
        _uiState.update {
            it.copy(
                bitWidthMode = mode,
                errorMessage = null
            )
        }
        if (_uiState.value.input.isNotBlank()) {
            analyze()
        }
    }

    fun onCustomWidthInputChange(widthStr: String) {
        _uiState.update { it.copy(customWidthInput = widthStr) }
    }

    fun applyCustomWidth() {
        val widthValidation = BitCountValidator.validateCustomWidth(_uiState.value.customWidthInput)
        if (widthValidation.isFailure) {
            hapticHelper.performError(_uiState.value.settings.hapticFeedback)
            _uiState.update {
                it.copy(
                    errorMessage = widthValidation.exceptionOrNull()?.message
                        ?: "Enter a bit width from 1 to 2000."
                )
            }
            return
        }

        val width = widthValidation.getOrThrow()
        onBitWidthModeChange(BitWidthMode.Fixed(width))
    }

    fun analyze() {
        val state = _uiState.value
        val validation = BitCountValidator.validateInput(state.input, state.inputBase)
        if (validation.isFailure) {
            hapticHelper.performError(state.settings.hapticFeedback)
            _uiState.update {
                it.copy(
                    errorMessage = validation.exceptionOrNull()?.message ?: "Invalid input",
                    result = null
                )
            }
            return
        }

        _uiState.update { it.copy(isCalculating = true, errorMessage = null) }

        val analysisResult = analyzer.analyze(
            input = state.input,
            inputBase = state.inputBase,
            bitWidthMode = state.bitWidthMode
        )

        if (analysisResult.isSuccess) {
            hapticHelper.performLightClick(state.settings.hapticFeedback)
            val result = analysisResult.getOrThrow()
            _uiState.update {
                it.copy(
                    result = result,
                    errorMessage = null,
                    isCalculating = false,
                    bitPositionResult = null,
                    bitPositionError = null
                )
            }
            settingsRepository.saveLastInputState(state.input, state.inputBase, state.bitWidthMode)
        } else {
            hapticHelper.performError(state.settings.hapticFeedback)
            _uiState.update {
                it.copy(
                    errorMessage = analysisResult.exceptionOrNull()?.message ?: "Analysis failed",
                    result = null,
                    isCalculating = false
                )
            }
        }
    }

    fun onBitPositionInputChange(posStr: String) {
        _uiState.update {
            it.copy(
                bitPositionInput = posStr,
                bitPositionError = null
            )
        }
    }

    fun checkBitPosition() {
        val state = _uiState.value
        val result = state.result
        if (result == null) {
            _uiState.update { it.copy(bitPositionError = "Analyze a number first.") }
            return
        }

        val validation = BitCountValidator.validateBitPosition(
            state.bitPositionInput,
            result.analyzedBitLength
        )

        if (validation.isFailure) {
            hapticHelper.performError(state.settings.hapticFeedback)
            _uiState.update {
                it.copy(
                    bitPositionError = validation.exceptionOrNull()?.message
                        ?: "Bit position is outside the current binary length.",
                    bitPositionResult = null
                )
            }
            return
        }

        val pos = validation.getOrThrow()
        // Extract bit value
        val magnitudeBigInt = BigInteger(result.magnitudeBinary, 2)
        val bitCheckResult = analyzer.checkBit(pos, magnitudeBigInt, result.analyzedBitLength)

        if (bitCheckResult.isSuccess) {
            hapticHelper.performTick(state.settings.hapticFeedback)
            _uiState.update {
                it.copy(
                    bitPositionResult = bitCheckResult.getOrThrow(),
                    bitPositionError = null
                )
            }
        } else {
            hapticHelper.performError(state.settings.hapticFeedback)
            _uiState.update {
                it.copy(
                    bitPositionError = bitCheckResult.exceptionOrNull()?.message
                        ?: "Bit position is outside the current binary length.",
                    bitPositionResult = null
                )
            }
        }
    }

    fun saveCurrentAnalysis() {
        val state = _uiState.value
        if (state.result == null || state.input.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "No analysis to save.") }
            return
        }

        val added = historyRepository.addHistory(
            inputBase = state.inputBase,
            input = state.input,
            bitWidth = state.bitWidthMode
        )

        hapticHelper.performLightClick(state.settings.hapticFeedback)
        _uiState.update {
            it.copy(
                snackbarMessage = if (added) "Saved to history." else "Already saved."
            )
        }
    }

    fun restoreHistory(item: BitCountHistory) {
        hapticHelper.performLightClick(_uiState.value.settings.hapticFeedback)
        _uiState.update {
            it.copy(
                inputBase = item.inputBase,
                input = item.input,
                bitWidthMode = item.bitWidth,
                errorMessage = null,
                bitPositionInput = "",
                bitPositionResult = null,
                bitPositionError = null
            )
        }
        analyze()
    }

    fun deleteHistoryItem(id: String) {
        hapticHelper.performLightClick(_uiState.value.settings.hapticFeedback)
        historyRepository.deleteHistory(id)
    }

    fun clearAllHistory() {
        hapticHelper.performLightClick(_uiState.value.settings.hapticFeedback)
        historyRepository.clearAllHistory()
    }

    fun clear() {
        hapticHelper.performLightClick(_uiState.value.settings.hapticFeedback)
        _uiState.update {
            it.copy(
                input = "",
                result = null,
                errorMessage = null,
                bitPositionInput = "",
                bitPositionResult = null,
                bitPositionError = null
            )
        }
    }

    fun triggerHapticClick() {
        hapticHelper.performLightClick(_uiState.value.settings.hapticFeedback)
    }

    fun triggerHapticTick() {
        hapticHelper.performTick(_uiState.value.settings.hapticFeedback)
    }

    fun clearSnackbarMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    // Settings modifiers
    fun updateTheme(theme: AppTheme) = settingsRepository.updateTheme(theme)
    fun updateDynamicColor(enabled: Boolean) = settingsRepository.updateDynamicColor(enabled)
    fun updateHapticFeedback(enabled: Boolean) = settingsRepository.updateHapticFeedback(enabled)
    fun updateAnimations(enabled: Boolean) = settingsRepository.updateAnimations(enabled)
    fun updateConfirmDelete(enabled: Boolean) = settingsRepository.updateConfirmDelete(enabled)
    fun updateGrouping(grouping: BinaryGrouping) = settingsRepository.updateGrouping(grouping)
    fun updateShowBitPositions(show: Boolean) = settingsRepository.updateShowBitPositions(show)
    fun updateBinaryPrefixInCopy(enabled: Boolean) = settingsRepository.updateBinaryPrefixInCopy(enabled)
    fun resetSettings() = settingsRepository.resetSettings()
}
