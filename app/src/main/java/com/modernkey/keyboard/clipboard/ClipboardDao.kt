package com.modernkey.keyboard.clipboard

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipboardDao {
    @Query("SELECT * FROM clipboard_items ORDER BY isPinned DESC, timestamp DESC LIMIT :limit")
    fun getAll(limit: Int = 20): Flow<List<ClipboardItem>>

    @Query("SELECT * FROM clipboard_items WHERE isPinned = 1 ORDER BY timestamp DESC")
    fun getPinned(): Flow<List<ClipboardItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ClipboardItem): Long

    @Update
    suspend fun update(item: ClipboardItem)

    @Delete
    suspend fun delete(item: ClipboardItem)

    @Query("DELETE FROM clipboard_items WHERE isPinned = 0")
    suspend fun deleteUnpinned()

    @Query("DELETE FROM clipboard_items")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM clipboard_items WHERE isPinned = 0")
    suspend fun countUnpinned(): Int

    @Query("DELETE FROM clipboard_items WHERE id IN (SELECT id FROM clipboard_items WHERE isPinned = 0 ORDER BY timestamp ASC LIMIT :count)")
    suspend fun deleteOldest(count: Int)
}
