package com.netraplus.calculator

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages calculator settings and history.
 */
class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("TalkingCalculator", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_HISTORY = "calc_history"
        private const val MAX_HISTORY_ENTRIES = 50
    }
    
    /**
     * Add a history entry.
     */
    fun addHistoryEntry(entry: String) {
        val history = getHistory().toMutableList()
        history.add(0, entry) // Add to beginning
        
        // Keep only last MAX_HISTORY_ENTRIES
        if (history.size > MAX_HISTORY_ENTRIES) {
            history.removeAt(history.size - 1)
        }
        
        prefs.edit()
            .putString(KEY_HISTORY, history.joinToString("|||"))
            .apply()
    }
    
    /**
     * Get calculation history.
     */
    fun getHistory(): List<String> {
        val historyStr = prefs.getString(KEY_HISTORY, "") ?: ""
        return if (historyStr.isEmpty()) {
            emptyList()
        } else {
            historyStr.split("|||")
        }
    }
    
    /**
     * Clear history.
     */
    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }
}

