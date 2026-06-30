package com.bistu.focuslist.ui.template

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bistu.focuslist.R
import com.bistu.focuslist.databinding.ActivityTemplateListBinding
import com.google.android.material.snackbar.Snackbar

class TemplateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTemplateListBinding
    private val viewModel: TemplateViewModel by viewModels()
    private lateinit var adapter: TemplateAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTemplateListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.online_templates)

        adapter = TemplateAdapter { template ->
            viewModel.importTemplate(template)
        }
        binding.recyclerTemplates.layoutManager = LinearLayoutManager(this)
        binding.recyclerTemplates.adapter = adapter

        // 下拉刷新
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        binding.btnRetry.setOnClickListener { viewModel.refresh() }

        viewModel.uiState.observe(this) { render(it) }

        // 观察正在导入的 id 集合，更新 Adapter 状态
        viewModel.importingIds.observe(this) { ids ->
            adapter.updateImportingIds(ids)
        }
    }

    private fun render(state: TemplateUiState) {
        binding.progress.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        binding.btnRetry.isEnabled = !state.isLoading
        // SwipeRefresh 的刷新指示器与 isLoading 保持一致
        binding.swipeRefresh.isRefreshing = state.isLoading

        adapter.submitList(state.templates)
        binding.textStatus.text = if (state.fromNetwork) {
            getString(R.string.template_source_online)
        } else {
            getString(R.string.template_source_local)
        }
        if (state.message.isNotBlank()) {
            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG)
                .setAction("重试") { viewModel.refresh() }
                .show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
