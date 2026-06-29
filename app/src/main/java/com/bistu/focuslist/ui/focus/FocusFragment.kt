package com.bistu.focuslist.ui.focus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bistu.focuslist.R
import com.bistu.focuslist.data.Task
import com.bistu.focuslist.databinding.FragmentFocusBinding
import com.bistu.focuslist.service.FocusTimerService
import com.bistu.focuslist.service.FocusUiState
import com.bistu.focuslist.util.Prefs
import com.bistu.focuslist.util.TimeUtils

/**
 * 专注页面（番茄钟）。
 * 选择时长与关联任务后开始专注；实时显示倒计时（来自前台服务的 LiveData）；
 * 顶部卡片展示通过网络获取的“每日一言”。
 */
class FocusFragment : Fragment() {

    private var _binding: FragmentFocusBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FocusViewModel by viewModels()
    private var pendingTasks: List<Task> = emptyList()
    private var focusModeLabel = "自定义"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFocusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 时长滑块
        val defaultMinutes = Prefs.getFocusMinutes(requireContext())
        binding.sliderDuration.value = defaultMinutes.toFloat().coerceIn(5f, 60f)
        binding.sliderDuration.addOnChangeListener { _, value, _ ->
            if (FocusTimerService.state.value?.running != true) {
                focusModeLabel = getString(R.string.preset_custom)
                updateIdlePreview(value.toInt())
            }
        }

        // 任务下拉框（默认仅“自由专注”）
        setSpinnerItems(listOf(getString(R.string.free_focus)))

        binding.btnStart.setOnClickListener {
            val minutes = binding.sliderDuration.value.toInt()
            Prefs.setFocusMinutes(requireContext(), minutes)
            val (taskId, taskTitle) = selectedTask()
            FocusTimerService.startFocus(
                requireContext(),
                minutes,
                taskId,
                taskTitle,
                focusModeLabel
            )
        }
        binding.btnPresetPomodoro.setOnClickListener {
            applyPreset(minutes = 25, label = getString(R.string.preset_pomodoro))
        }
        binding.btnPresetDeep.setOnClickListener {
            applyPreset(minutes = 50, label = getString(R.string.preset_deep))
        }
        binding.btnShortBreak.setOnClickListener {
            FocusTimerService.startBreak(requireContext(), minutes = 5, longBreak = false)
        }
        binding.btnLongBreak.setOnClickListener {
            FocusTimerService.startBreak(requireContext(), minutes = 15, longBreak = true)
        }
        binding.btnPause.setOnClickListener {
            FocusTimerService.sendAction(requireContext(), FocusTimerService.ACTION_PAUSE)
        }
        binding.btnResume.setOnClickListener {
            FocusTimerService.sendAction(requireContext(), FocusTimerService.ACTION_RESUME)
        }
        binding.btnStop.setOnClickListener {
            FocusTimerService.sendAction(requireContext(), FocusTimerService.ACTION_STOP)
        }
        binding.btnRefreshQuote.setOnClickListener { viewModel.fetchQuote() }

        // 观察任务列表
        viewModel.pendingTasks.observe(viewLifecycleOwner) { tasks ->
            pendingTasks = tasks
            val items = mutableListOf(getString(R.string.free_focus))
            items.addAll(tasks.map { it.title })
            setSpinnerItems(items)
        }

        // 观察每日一言
        viewModel.quote.observe(viewLifecycleOwner) { q ->
            binding.textQuote.text = q.text
            binding.textQuoteFrom.text = if (q.from.isNotBlank()) "—— ${q.from}" else ""
        }

        // 观察番茄钟实时状态
        FocusTimerService.state.observe(viewLifecycleOwner) { render(it) }
    }

    private fun render(state: FocusUiState) {
        if (state.running) {
            binding.layoutIdle.visibility = View.GONE
            binding.layoutRunning.visibility = View.VISIBLE

            binding.progressRing.max = state.totalSeconds.coerceAtLeast(1)
            binding.progressRing.setProgressCompat(state.remainingSeconds, true)
            binding.textTime.text = TimeUtils.formatMmSs(state.remainingSeconds)
            binding.textFocusTask.text = when (state.mode) {
                FocusTimerService.MODE_SHORT_BREAK -> getString(R.string.short_break)
                FocusTimerService.MODE_LONG_BREAK -> getString(R.string.long_break)
                else -> state.taskTitle.ifBlank { getString(R.string.free_focus) }
            }

            binding.btnPause.visibility = if (state.paused) View.GONE else View.VISIBLE
            binding.btnResume.visibility = if (state.paused) View.VISIBLE else View.GONE
            binding.textStatusHint.text = when {
                state.paused -> getString(R.string.focus_paused)
                state.mode == FocusTimerService.MODE_SHORT_BREAK ||
                    state.mode == FocusTimerService.MODE_LONG_BREAK -> getString(R.string.break_running)
                else -> getString(R.string.focus_running)
            }
        } else {
            binding.layoutIdle.visibility = View.VISIBLE
            binding.layoutRunning.visibility = View.GONE
            updateIdlePreview(binding.sliderDuration.value.toInt())
            binding.textStatusHint.text = getString(R.string.focus_idle_hint)
        }
    }

    private fun updateIdlePreview(minutes: Int) {
        val totalSec = minutes * 60
        binding.progressRing.max = totalSec.coerceAtLeast(1)
        binding.progressRing.setProgressCompat(totalSec, false)
        binding.textTime.text = TimeUtils.formatMmSs(totalSec)
        binding.textFocusTask.text = getString(R.string.minutes_focus_fmt, minutes)
    }

    private fun applyPreset(minutes: Int, label: String) {
        binding.sliderDuration.value = minutes.toFloat().coerceIn(5f, 60f)
        focusModeLabel = label
        updateIdlePreview(minutes)
    }

    private fun setSpinnerItems(items: List<String>) {
        val adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, items
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTask.adapter = adapter
    }

    private fun selectedTask(): Pair<Long?, String> {
        val pos = binding.spinnerTask.selectedItemPosition
        return if (pos <= 0 || pos - 1 >= pendingTasks.size) {
            null to ""
        } else {
            val t = pendingTasks[pos - 1]
            t.id to t.title
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
