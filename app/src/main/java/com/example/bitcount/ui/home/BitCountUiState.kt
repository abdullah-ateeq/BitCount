package com.example.bitcount.ui.home

import com.example.bitcount.model.AppSettings
import com.example.bitcount.model.BitCountHistory
import com.example.bitcount.model.BitCountResult
import com.example.bitcount.model.BitWidthMode
import com.example.bitcount.model.InputBase

data class BitCountUiState(
    val inputBase: InputBase = InputBase.DECIMAL,

    val input: String = "",

    val bitWidthMode: BitWidthMode = BitWidthMode.Natural,

    val result: BitCountResult? = null,

    val bitPositionInput: String = "",

    val bitPositionResult: Int? = null,

    val errorMessage: String? = null,

    val bitPositionError: String? = null,

    val isCalculating: Boolean = false,

    val history: List<BitCountHistory> = emptyList(),

    // Extra UI helpers
    val customWidthInput: String = "128",
    val snackbarMessage: String? = null,
    val settings: AppSettings = AppSettings()
)
