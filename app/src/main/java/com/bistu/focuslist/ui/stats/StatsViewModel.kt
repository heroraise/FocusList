package com.bistu.focuslist.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.bistu.focuslist.data.FocusSession
import com.bistu.focuslist.data.Repository
import com.bistu.focuslist.util.TimeUtils

/**
 * 统计页 ViewModel。
 * 汇总今日 / 累计的专注时长与番茄个数，以及最近的专注记录。
 */
class StatsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = Repository.get(app)
    private val startOfToday = TimeUtils.startOfToday()
    private val startOfRecentSessions = TimeUtils.startOfRecentDays(RECENT_SESSION_DAYS)

    val todayMinutes: LiveData<Int> = repo.observeTodayMinutes(startOfToday)
    val todayCount: LiveData<Int> = repo.observeTodayCount(startOfToday)
    val totalMinutes: LiveData<Int> = repo.observeTotalMinutes()
    val totalCount: LiveData<Int> = repo.observeTotalCount()
    val pendingCount: LiveData<Int> = repo.observePendingCount()
    val recentSessions: LiveData<List<FocusSession>> =
        repo.observeRecentSessionsSince(startOfRecentSessions, RECENT_SESSION_LIMIT)

    companion object {
        private const val RECENT_SESSION_DAYS = 7
        private const val RECENT_SESSION_LIMIT = 30
    }
}
