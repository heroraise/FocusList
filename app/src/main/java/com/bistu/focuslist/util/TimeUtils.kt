package com.bistu.focuslist.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** 时间相关的工具函数。 */
object TimeUtils {

    private val dateTimeFmt = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

    /** 今天 0 点的时间戳 */
    fun startOfToday(): Long {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    /** 最近 days 天的起点（包含今天），例如 7 天表示从 6 天前 0 点开始。 */
    fun startOfRecentDays(days: Int): Long {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        c.add(Calendar.DAY_OF_YEAR, -(days.coerceAtLeast(1) - 1))
        return c.timeInMillis
    }

    fun formatDateTime(millis: Long): String = dateTimeFmt.format(Date(millis))

    fun formatTime(millis: Long): String = timeFmt.format(Date(millis))

    /** 把秒数格式化为 mm:ss */
    fun formatMmSs(totalSeconds: Int): String {
        val m = totalSeconds / 60
        val s = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", m, s)
    }
}
