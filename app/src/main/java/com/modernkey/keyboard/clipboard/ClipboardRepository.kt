package com.modernkey.keyboard.clipboard

import kotlinx.coroutines.flow.Flow

class ClipboardRepository(private val dao: ClipboardDao) {

    companion object {
        const val DEFAULT_MAX = 20
    }

    fun getAllItems(limit: Int = DEFAULT_MAX): Flow<List<ClipboardItem>> = dao.getAll(limit)

    suspend fun addItem(content: String) {
        if (content.isBlank()) return
        val type = detectType(content)
        val item = ClipboardItem(content = content, type = type)
        dao.insert(item)
        // Keep max items
        val count = dao.countUnpinned()
        if (count > DEFAULT_MAX) {
            dao.deleteOldest(count - DEFAULT_MAX)
        }
    }

    suspend fun togglePin(item: ClipboardItem) {
        dao.update(item.copy(isPinned = !item.isPinned))
    }

    suspend fun deleteItem(item: ClipboardItem) = dao.delete(item)

    suspend fun clearAll() = dao.deleteAll()

    suspend fun clearUnpinned() = dao.deleteUnpinned()

    private fun detectType(content: String): ClipType {
        return when {
            content.matches(Regex("^(https?://|www\\.).*")) -> ClipType.URL
            content.matches(Regex("^[+]?[\\d\\s\\-()]{7,15}$")) -> ClipType.PHONE
            content.matches(Regex("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$")) -> ClipType.EMAIL
            else -> ClipType.TEXT
        }
    }
}
