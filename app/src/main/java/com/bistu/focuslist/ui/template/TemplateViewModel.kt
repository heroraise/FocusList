package com.bistu.focuslist.ui.template

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bistu.focuslist.data.StudyTemplateRepository
import com.bistu.focuslist.network.StudyTemplate
import kotlinx.coroutines.launch

class TemplateViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = StudyTemplateRepository.get(app)

    private val _uiState = MutableLiveData(TemplateUiState(isLoading = true))
    val uiState: LiveData<TemplateUiState> = _uiState

    // 正在导入的模板 id 集合，用于在 UI 上显示导入中状态
    private val _importingIds = MutableLiveData<Set<String>>(emptySet())
    val importingIds: LiveData<Set<String>> = _importingIds

    init {
        refresh()
    }

    fun refresh() {
        _uiState.value = _uiState.value.orEmpty().copy(isLoading = true, message = "")
        viewModelScope.launch {
            val result = repo.loadTemplates()
            _uiState.value = TemplateUiState(
                isLoading = false,
                templates = result.templates,
                fromNetwork = result.fromNetwork,
                message = if (result.fromNetwork) "已加载在线模板" else "网络不可用，已使用本地模板"
            )
        }
    }

    fun importTemplate(template: StudyTemplate) {
        // 标记为正在导入
        _importingIds.value = _importingIds.value.orEmpty() + template.id

        viewModelScope.launch {
            try {
                val count = repo.importTemplate(template)
                _uiState.value = _uiState.value.orEmpty().copy(
                    message = "已导入 $count 个任务"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.orEmpty().copy(
                    message = "导入失败: ${e.message ?: "未知错误"}"
                )
            } finally {
                // 移除导入中标记
                _importingIds.value = _importingIds.value.orEmpty() - template.id
            }
        }
    }

    private fun TemplateUiState?.orEmpty(): TemplateUiState = this ?: TemplateUiState()
}

data class TemplateUiState(
    val isLoading: Boolean = false,
    val templates: List<StudyTemplate> = emptyList(),
    val fromNetwork: Boolean = false,
    val message: String = ""
)
