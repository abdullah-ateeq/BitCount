package com.example.bitcount.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.bitcount.model.BitCountHistory
import com.example.bitcount.model.BitWidthMode
import com.example.bitcount.model.InputBase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class HistoryRepository(
    context: Context? = null,
    sharedPreferences: SharedPreferences? = null
) {

    private val prefs: SharedPreferences = sharedPreferences
        ?: context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        ?: throw IllegalArgumentException("Either context or sharedPreferences must be provided")

    private val _historyFlow = MutableStateFlow<List<BitCountHistory>>(loadHistory())
    val historyFlow: StateFlow<List<BitCountHistory>> = _historyFlow.asStateFlow()

    fun addHistory(
        inputBase: InputBase,
        input: String,
        bitWidth: BitWidthMode
    ): Boolean {
        val current = _historyFlow.value
        val trimmedInput = input.trim()

        // Check consecutive duplicate
        val first = current.firstOrNull()
        if (first != null &&
            first.inputBase == inputBase &&
            first.input == trimmedInput &&
            first.bitWidth == bitWidth
        ) {
            return false // Duplicate avoided
        }

        val newItem = BitCountHistory(
            id = UUID.randomUUID().toString(),
            inputBase = inputBase,
            input = trimmedInput,
            bitWidth = bitWidth,
            savedAt = System.currentTimeMillis()
        )

        // Prepend new item and keep at most MAX_HISTORY entries
        val updated = (listOf(newItem) + current).take(MAX_HISTORY)
        saveHistory(updated)
        _historyFlow.value = updated
        return true
    }

    fun deleteHistory(id: String) {
        val updated = _historyFlow.value.filterNot { it.id == id }
        saveHistory(updated)
        _historyFlow.value = updated
    }

    fun clearAllHistory() {
        saveHistory(emptyList())
        _historyFlow.value = emptyList()
    }

    private fun loadHistory(): List<BitCountHistory> {
        val raw = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<BitCountHistory>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.getString("id")
                val base = InputBase.valueOf(obj.getString("base"))
                val input = obj.getString("input")
                val widthType = obj.getString("widthType")
                val bitWidth = if (widthType == "FIXED") {
                    BitWidthMode.Fixed(obj.getInt("width"))
                } else {
                    BitWidthMode.Natural
                }
                val savedAt = obj.getLong("savedAt")
                list.add(BitCountHistory(id, base, input, bitWidth, savedAt))
            }
            list.take(MAX_HISTORY)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveHistory(items: List<BitCountHistory>) {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("base", item.inputBase.name)
            obj.put("input", item.input)
            when (item.bitWidth) {
                is BitWidthMode.Natural -> {
                    obj.put("widthType", "NATURAL")
                }
                is BitWidthMode.Fixed -> {
                    obj.put("widthType", "FIXED")
                    obj.put("width", item.bitWidth.width)
                }
            }
            obj.put("savedAt", item.savedAt)
            array.put(obj)
        }
        prefs.edit().putString(KEY_HISTORY, array.toString()).apply()
    }

    companion object {
        private const val PREFS_NAME = "bitcount_history_prefs"
        private const val KEY_HISTORY = "saved_history"
        const val MAX_HISTORY = 10
    }
}
