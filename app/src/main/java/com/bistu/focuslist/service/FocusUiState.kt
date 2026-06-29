package com.bistu.focuslist.service

/**
 * 番茄钟服务向界面发布的实时状态。
 */
data class FocusUiState(
    /** 是否正在一个专注会话中（包含暂停） */
    val running: Boolean = false,
    /** 是否已暂停 */
    val paused: Boolean = false,
    /** 本次专注总秒数 */
    val totalSeconds: Int = 0,
    /** 剩余秒数 */
    val remainingSeconds: Int = 0,
    /** 关联任务标题 */
    val taskTitle: String = "",
    /** 关联任务 id */
    val taskId: Long? = null,
    /** 当前计时类型：专注 / 短休息 / 长休息 */
    val mode: String = FocusTimerService.MODE_FOCUS
)
