package com.modernkey.keyboard.emoji

import com.modernkey.keyboard.ModernKeyApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EmojiRepository private constructor() {

    companion object {
        val instance: EmojiRepository by lazy { EmojiRepository() }
        private const val RECENT_KEY = "recent_emojis"
        private const val MAX_RECENT = 30
    }

    private val prefs by lazy {
        ModernKeyApp.instance.getSharedPreferences("emoji_prefs", android.content.Context.MODE_PRIVATE)
    }

    val allEmojis: List<EmojiItem> = EmojiDataSource.allEmojis

    fun getByCategory(category: EmojiCategory): List<EmojiItem> {
        if (category == EmojiCategory.RECENT) return getRecent()
        return allEmojis.filter { it.category == category }
    }

    fun search(query: String): List<EmojiItem> {
        if (query.isBlank()) return emptyList()
        val q = query.lowercase()
        return allEmojis.filter { emoji ->
            emoji.name.lowercase().contains(q) ||
            emoji.keywords.any { it.lowercase().contains(q) } ||
            emoji.emoji.contains(q)
        }
    }

    fun getRecent(): List<EmojiItem> {
        val recentStr = prefs.getString(RECENT_KEY, "") ?: ""
        if (recentStr.isEmpty()) return emptyList()
        val recentEmojis = recentStr.split(",").filter { it.isNotEmpty() }
        return recentEmojis.mapNotNull { emoji ->
            allEmojis.find { it.emoji == emoji }
        }
    }

    fun addToRecent(emoji: EmojiItem) {
        val recent = getRecent().toMutableList()
        recent.removeAll { it.emoji == emoji.emoji }
        recent.add(0, emoji)
        if (recent.size > MAX_RECENT) {
            recent.removeAt(recent.lastIndex)
        }
        val serialized = recent.joinToString(",") { it.emoji }
        prefs.edit().putString(RECENT_KEY, serialized).apply()
    }

    fun clearRecent() {
        prefs.edit().remove(RECENT_KEY).apply()
    }
}
