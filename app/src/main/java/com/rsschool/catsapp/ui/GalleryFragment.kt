package com.rsschool.catsapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.rsschool.catsapp.databinding.FragmentGalleryBinding
import com.rsschool.catsapp.model.Cat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GalleryFragment : Fragment() {
    private var binding: FragmentGalleryBinding? = null
    private val viewModel by viewModels<CatsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding?.root!! // todo
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val catsAdapter = CatsGalleryAdapter(object : CatsGalleryAdapter.OnImageClickListener {
            override fun onItemClick(image: Cat) {
                val action = GalleryFragmentDirections.actionFragmentGalleryToImageDetailsFragment(image)
                findNavController().navigate(action)
            }
        })

        binding?.apply {
            listRecyclerView.layoutManager = GridLayoutManager(context, 2)
            listRecyclerView.setHasFixedSize(true)
            listRecyclerView.adapter = catsAdapter.withLoadStateHeaderAndFooter(
                footer = CatsGalleryLoadingStateAdapter { catsAdapter.retry() },
                header = CatsGalleryLoadingStateAdapter { catsAdapter.retry() }
            )
            retryButton.setOnClickListener {
                catsAdapter.retry()
            }
        }
        lifecycleScope.launch {
            viewModel.cats.observe(viewLifecycleOwner) {
                catsAdapter.submitData(viewLifecycleOwner.lifecycle, it)
                }
        }
        catsAdapter.addLoadStateListener { loadState ->
            binding?.apply {
                progressBar.isVisible = loadState.source.refresh is LoadState.Loading
                listRecyclerView.isVisible = loadState.source.refresh is LoadState.NotLoading
                retryButton.isVisible = loadState.source.refresh is LoadState.Error
                // empty view
                if (loadState.source.refresh is LoadState.NotLoading &&
                    catsAdapter.itemCount < 1) {
                    listRecyclerView.isVisible = false
                    noResultsText.isVisible = true
                    retryButton.isVisible = true
                } else {
                    noResultsText.isVisible = false
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
