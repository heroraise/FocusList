package com.bistu.focuslist.ui.tasks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bistu.focuslist.data.Repository
import com.bistu.focuslist.data.Task
import com.bistu.focuslist.util.AlarmScheduler
import com.bistu.focuslist.widget.TaskWidgetProvider
import kotlinx.coroutines.launch

/**
 * 任务列表 ViewModel。
 * 负责任务的增删改查、搜索筛选，并在数据变化后同步刷新闹钟与桌面小组件。
 */
class TaskViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = Repository.get(app)

    private val searchQuery = MutableLiveData("")
    private val filterCategory = MutableLiveData("")
    private val filterPriority = MutableLiveData(-1)
    private val filterDueOnly = MutableLiveData(false)
    private val filteredTasks = MediatorLiveData<List<Task>>()
    private var currentTaskSource: LiveData<List<Task>>? = null

    val tasks: LiveData<List<Task>> = filteredTasks

    init {
        filteredTasks.addSource(searchQuery) { refreshTaskSource() }
        filteredTasks.addSource(filterCategory) { refreshTaskSource() }
        filteredTasks.addSource(filterPriority) { refreshTaskSource() }
        filteredTasks.addSource(filterDueOnly) { refreshTaskSource() }
        refreshTaskSource()
    }

    private fun refreshTaskSource() {
        currentTaskSource?.let { filteredTasks.removeSource(it) }
        val source = repo.observeFilteredTasks(
            query = searchQuery.value.orEmpty(),
            category = filterCategory.value.orEmpty(),
            priority = filterPriority.value ?: -1,
            dueOnly = filterDueOnly.value ?: false
        )
        filteredTasks.addSource(source) { filteredTasks.value = it }
        currentTaskSource = source
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query.trim()
    }

    fun setFilterCategory(category: String) {
        filterCategory.value = category
    }

    fun setFilterPriority(priority: Int) {
        filterPriority.value = priority
    }

    fun setFilterDueOnly(dueOnly: Boolean) {
        filterDueOnly.value = dueOnly
    }

    fun clearFilterOptions() {
        filterCategory.value = ""
        filterPriority.value = -1
        filterDueOnly.value = false
    }

    /** 勾选 / 取消勾选完成状态 */
    fun toggleDone(task: Task) {
        viewModelScope.launch {
            val updated = task.copy(isDone = !task.isDone)
            repo.updateTask(updated)
            val ctx = getApplication<Application>()
            if (updated.isDone) {
                AlarmScheduler.cancel(ctx, updated.id)
            } else {
                AlarmScheduler.schedule(ctx, updated)
            }
            TaskWidgetProvider.notifyRefresh(ctx)
        }
    }

    fun delete(task: Task) {
        viewModelScope.launch {
            repo.deleteTask(task)
            val ctx = getApplication<Application>()
            AlarmScheduler.cancel(ctx, task.id)
            TaskWidgetProvider.notifyRefresh(ctx)
        }
    }

    fun clearCompleted() {
        viewModelScope.launch {
            repo.clearCompletedTasks()
            TaskWidgetProvider.notifyRefresh(getApplication())
        }
    }

    /** 撤销删除时重新插入 */
    fun insert(task: Task) {
        viewModelScope.launch {
            val newId = repo.insertTask(task.copy(id = 0))
            val ctx = getApplication<Application>()
            AlarmScheduler.schedule(ctx, task.copy(id = newId))
            TaskWidgetProvider.notifyRefresh(ctx)
        }
    }
}
