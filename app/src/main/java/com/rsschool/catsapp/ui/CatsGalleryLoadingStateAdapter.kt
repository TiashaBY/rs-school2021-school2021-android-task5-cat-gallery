package com.rsschool.catsapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rsschool.catsapp.databinding.LoadingStateLayoutBinding

class CatsGalleryLoadingStateAdapter(private val retry: () -> Unit) :
    LoadStateAdapter<CatsGalleryLoadingStateAdapter.LoadingStateViewHolder>() {

    inner class LoadingStateViewHolder(private val binding: LoadingStateLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.retryButton.setOnClickListener {
                retry.invoke()
            }
        }

        fun bind(loadState: LoadState) {
            if (loadState is LoadState.Loading) {
                binding.progressBar.isVisible = true
                binding.retryButton.isVisible = false
            } else if (loadState is LoadState.Error) {
                binding.retryButton.isVisible = true
                binding.progressBar.isVisible = false
            }
        }
    }

    override fun onBindViewHolder(holder: LoadingStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): LoadingStateViewHolder {
        val binding =
            LoadingStateLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LoadingStateViewHolder(binding)
    }
}
