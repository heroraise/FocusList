package com.bistu.focuslist.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 番茄钟专注记录。对应数据库表 focus_sessions。
 * 用于统计页展示每日 / 累计专注时长。
 */
@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** 关联的任务 id，可为空（自由专注） */
    val taskId: Long? = null,

    /** 关联任务标题快照（便于历史展示） */
    val taskTitle: String = "",

    /** 本次专注时长（分钟） */
    val durationMinutes: Int,

    /** 开始时间戳（毫秒） */
    val startTime: Long,

    /** 结束时间戳（毫秒） */
    val endTime: Long,

    /** 是否完整完成（true=自然结束，false=中途放弃） */
    val completed: Boolean = true,

    /** 专注结束后的心情复盘，如“顺利”“一般”“分心” */
    val reviewMood: String = "",

    /** 打断原因，如“手机消息”“环境干扰”“任务太难” */
    val interruptionReason: String = "",

    /** 用户补充的复盘备注 */
    val reviewNotes: String = "",

    /** 本次专注模式，如 25/5、50/10、自定义 */
    val focusMode: String = "自定义"
)
