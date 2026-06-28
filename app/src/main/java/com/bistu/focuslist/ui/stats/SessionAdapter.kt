package com.bistu.focuslist.ui.stats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bistu.focuslist.R
import com.bistu.focuslist.data.FocusSession
import com.bistu.focuslist.databinding.ItemSessionBinding
import com.bistu.focuslist.util.TimeUtils

/**
 * 专注记录适配器（适配器视图）。
 */
class SessionAdapter : ListAdapter<FocusSession, SessionAdapter.SessionViewHolder>(DIFF) {

    inner class SessionViewHolder(val binding: ItemSessionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val binding = ItemSessionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SessionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val session = getItem(position)
        val ctx = holder.itemView.context
        with(holder.binding) {
            textSessionTitle.text = session.taskTitle.ifBlank { "自由专注" }
            textSessionTime.text = TimeUtils.formatDateTime(session.endTime)
            textSessionDuration.text = ctx.getString(R.string.minutes_fmt, session.durationMinutes)
            textSessionStatus.visibility = if (session.completed) View.GONE else View.VISIBLE
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<FocusSession>() {
            override fun areItemsTheSame(oldItem: FocusSession, newItem: FocusSession) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: FocusSession, newItem: FocusSession) =
                oldItem == newItem
        }
    }
}
