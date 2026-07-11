package com.modernkey.keyboard.clipboard

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ClipType { TEXT, URL, PHONE, EMAIL }

@Entity(tableName = "clipboard_items")
data class ClipboardItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val type: ClipType = ClipType.TEXT
)
