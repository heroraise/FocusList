package com.bistu.focuslist.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/** 专注记录 DAO。 */
@Dao
interface FocusSessionDao {

    @Insert
    suspend fun insert(session: FocusSession): Long

    @Query("SELECT * FROM focus_sessions ORDER BY endTime DESC, id DESC LIMIT :limit")
    fun observeRecent(limit: Int): LiveData<List<FocusSession>>

    @Query("SELECT * FROM focus_sessions WHERE endTime >= :since ORDER BY endTime DESC, id DESC LIMIT :limit")
    fun observeRecentSince(since: Long, limit: Int): LiveData<List<FocusSession>>

    /** 自某时间点以来的累计专注分钟数 */
    @Query("SELECT COALESCE(SUM(durationMinutes), 0) FROM focus_sessions WHERE startTime >= :since")
    fun observeTotalMinutesSince(since: Long): LiveData<Int>

    /** 自某时间点以来专注过的番茄个数（包含未完成但有记录的） */
    @Query("SELECT COUNT(*) FROM focus_sessions WHERE startTime >= :since")
    fun observeCountSince(since: Long): LiveData<Int>
}
