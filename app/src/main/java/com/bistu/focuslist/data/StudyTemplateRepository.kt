package com.bistu.focuslist.data

import android.content.Context
import com.bistu.focuslist.network.StudyTemplate
import com.bistu.focuslist.network.TemplateClient
import com.bistu.focuslist.network.TemplateTask

/**
 * 学习模板仓库。
 * 先尝试读取在线模板，失败时返回本地兜底模板，保证课程演示和离线使用都可用。
 */
class StudyTemplateRepository private constructor(context: Context) {

    private val taskRepository = Repository.get(context)

    suspend fun loadTemplates(): TemplateLoadResult {
        return try {
            val remote = TemplateClient.getTemplates().templates
                .filter { it.title.isNotBlank() && it.tasks.isNotEmpty() }
                .map { it.copy(source = it.source.ifBlank { SOURCE_REMOTE }) }
            if (remote.isNotEmpty()) {
                TemplateLoadResult(remote, fromNetwork = true)
            } else {
                TemplateLoadResult(localTemplates(), fromNetwork = false)
            }
        } catch (_: Exception) {
            TemplateLoadResult(localTemplates(), fromNetwork = false)
        }
    }

    suspend fun importTemplate(template: StudyTemplate): Int {
        val tasks = template.tasks.map { item ->
            Task(
                title = item.title,
                notes = item.notes,
                category = item.category.ifBlank { template.category },
                priority = item.priority.coerceIn(Task.PRIORITY_LOW, Task.PRIORITY_HIGH)
            )
        }
        return taskRepository.insertTasks(tasks).size
    }

    private fun localTemplates(): List<StudyTemplate> = listOf(
        StudyTemplate(
            id = "final-review",
            title = "期末周复习计划",
            category = "学习",
            description = "按课程梳理重点、刷题、回顾错题，适合 5-7 天复习周期。",
            estimatedDays = 7,
            source = SOURCE_LOCAL,
            tasks = listOf(
                TemplateTask("整理各科考试范围", "把老师划的重点和课件目录列出来。", "学习", Task.PRIORITY_HIGH),
                TemplateTask("复习第一门课程重点", "完成一轮知识点回顾并标记薄弱章节。", "学习", Task.PRIORITY_HIGH),
                TemplateTask("完成一套模拟题", "按考试时长计时完成，记录错题。", "学习", Task.PRIORITY_NORMAL),
                TemplateTask("错题二次复盘", "只看错题和不会的概念。", "学习", Task.PRIORITY_HIGH)
            )
        ),
        StudyTemplate(
            id = "english-vocab",
            title = "英语单词打卡计划",
            category = "学习",
            description = "每天背诵、复习、听写三步走，适合长期积累。",
            estimatedDays = 14,
            source = SOURCE_LOCAL,
            tasks = listOf(
                TemplateTask("背诵 30 个新单词", "用番茄钟完成一轮新词记忆。", "学习", Task.PRIORITY_NORMAL),
                TemplateTask("复习昨日单词", "遮住中文释义进行自测。", "学习", Task.PRIORITY_HIGH),
                TemplateTask("完成 10 分钟听写", "记录拼写错误并加入复习。", "学习", Task.PRIORITY_NORMAL)
            )
        ),
        StudyTemplate(
            id = "coding-practice",
            title = "编程练习计划",
            category = "工作",
            description = "从阅读题目、实现代码到复盘总结，适合算法或课程实验。",
            estimatedDays = 5,
            source = SOURCE_LOCAL,
            tasks = listOf(
                TemplateTask("阅读题目并拆解需求", "写下输入、输出、边界条件。", "工作", Task.PRIORITY_HIGH),
                TemplateTask("实现核心逻辑", "先保证主流程跑通，再处理边界。", "工作", Task.PRIORITY_HIGH),
                TemplateTask("补充测试用例", "覆盖正常、异常和边界数据。", "工作", Task.PRIORITY_NORMAL),
                TemplateTask("整理实现思路", "写下复杂度和踩坑点。", "工作", Task.PRIORITY_LOW)
            )
        )
    )

    data class TemplateLoadResult(
        val templates: List<StudyTemplate>,
        val fromNetwork: Boolean
    )

    companion object {
        private const val SOURCE_REMOTE = "在线模板"
        private const val SOURCE_LOCAL = "本地兜底"

        @Volatile
        private var INSTANCE: StudyTemplateRepository? = null

        fun get(context: Context): StudyTemplateRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: StudyTemplateRepository(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
