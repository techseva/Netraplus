package com.netraplus.data

import android.content.Context
import org.json.JSONArray

class CategoryRepository(private val context: Context) {
    fun load(category: String): List<ServiceItem> {
        val fileName = when (category) {
            "voice" -> "voice.json"
            "vision" -> "vision.json"
            "communication" -> "communication.json"
            "emergency" -> "emergency.json"
            "learning" -> "learning.json"
            "entertainment" -> "entertainment.json"
            "govsupport" -> "govsupport.json"
            // legacy mapping fallback
            "government" -> "government.json"
            "health" -> "health.json"
            "education" -> "education.json"
            "jobs" -> "jobs.json"
            "banking" -> "banking.json"
            "daily" -> "daily.json"
            else -> "voice.json"
        }
        val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
        val arr = JSONArray(json)
        val list = mutableListOf<ServiceItem>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                ServiceItem(
                    id = o.optString("id"),
                    title = o.optString("title"),
                    description = o.optString("description"),
                    ttsMessage = o.optString("ttsMessage", null)
                )
            )
        }
        return list
    }
}
