package com.bistu.focuslist.ui.review

import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bistu.focuslist.R
import com.bistu.focuslist.data.FocusSession
import com.bistu.focuslist.data.Repository
import com.bistu.focuslist.databinding.ActivityFocusReviewBinding
import com.bistu.focuslist.util.TimeUtils
import kotlinx.coroutines.launch

class FocusReviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFocusReviewBinding
    private val repo by lazy { Repository.get(this) }
    private var session: FocusSession? = null

    private val moods by lazy {
        listOf(
            getString(R.string.review_mood_good),
            getString(R.string.review_mood_normal),
            getString(R.string.review_mood_distracted)
        )
    }

    private val reasons by lazy {
        listOf(
            getString(R.string.review_reason_none),
            getString(R.string.review_reason_phone),
            getString(R.string.review_reason_environment),
            getString(R.string.review_reason_difficult)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFocusReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.focus_review)

        binding.spinnerMood.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            moods
        )
        binding.spinnerReason.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            reasons
        )

        binding.btnSaveReview.setOnClickListener { saveReview() }
        loadSession()
    }

    private fun loadSession() {
        val id = intent.getLongExtra(EXTRA_SESSION_ID, -1L)
        if (id <= 0) {
            finish()
            return
        }
        lifecycleScope.launch {
            val loaded = repo.getSession(id)
            session = loaded
            if (loaded == null) {
                finish()
            } else {
                render(loaded)
            }
        }
    }

    private fun render(session: FocusSession) {
        binding.textReviewSummary.text = getString(
            R.string.review_summary_fmt,
            session.taskTitle.ifBlank { getString(R.string.free_focus) },
            session.durationMinutes,
            session.focusMode,
            TimeUtils.formatDateTime(session.endTime)
        )
        if (session.reviewMood.isNotBlank()) {
            binding.spinnerMood.setSelection(moods.indexOf(session.reviewMood).coerceAtLeast(0))
        }
        if (session.interruptionReason.isNotBlank()) {
            binding.spinnerReason.setSelection(
                reasons.indexOf(session.interruptionReason).coerceAtLeast(0)
            )
        }
        binding.editReviewNotes.setText(session.reviewNotes)
    }

    private fun saveReview() {
        val current = session ?: return
        val updated = current.copy(
            reviewMood = moods.getOrElse(binding.spinnerMood.selectedItemPosition) { moods.first() },
            interruptionReason = reasons.getOrElse(binding.spinnerReason.selectedItemPosition) {
                reasons.first()
            },
            reviewNotes = binding.editReviewNotes.text?.toString()?.trim().orEmpty()
        )
        lifecycleScope.launch {
            repo.updateSession(updated)
            setResult(RESULT_OK)
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val EXTRA_SESSION_ID = "extra_session_id"
    }
}
