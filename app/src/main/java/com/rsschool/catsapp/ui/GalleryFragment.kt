package com.rsschool.catsapp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.rsschool.catsapp.databinding.FragmentGalleryBinding
import com.rsschool.catsapp.model.Cat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GalleryFragment : Fragment() {
    private var binding: FragmentGalleryBinding? = null
    private val viewModel by viewModels<CatsViewModel>()
    private val COLUMNS_NUMBER = 2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val catsAdapter = CatsGalleryAdapter(object : CatsGalleryAdapter.OnImageClickListener {
            override fun onItemClick(image: Cat) {
                val action =
                    GalleryFragmentDirections.actionFragmentGalleryToImageDetailsFragment(image)
                findNavController().navigate(action)
            }
        })

        val footer = CatsGalleryLoadingStateAdapter { catsAdapter.retry() }
        val header = CatsGalleryLoadingStateAdapter { catsAdapter.retry() }

        binding?.apply {
            listRecyclerView.layoutManager = GridLayoutManager(context, COLUMNS_NUMBER).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (position == catsAdapter.itemCount && (header.itemCount > 0 || footer.itemCount > 0)) {
                            COLUMNS_NUMBER
                        } else {
                            1
                        }
                    }
                }
                listRecyclerView.setHasFixedSize(true)
                listRecyclerView.adapter = catsAdapter.withLoadStateHeaderAndFooter(footer, header)
                retryButton.setOnClickListener {
                    catsAdapter.retry()
                }
            }
        }

        catsAdapter.addLoadStateListener { loadState ->
            binding?.apply {
                progressBar.isVisible = loadState.source.refresh is LoadState.Loading
                listRecyclerView.isVisible = loadState.source.refresh is LoadState.NotLoading
                retryButton.isVisible = loadState.source.refresh is LoadState.Error
                // empty view
                if (loadState.source.refresh is LoadState.NotLoading &&
                    catsAdapter.itemCount < 1
                ) {
                    listRecyclerView.isVisible = false
                    retryButton.isVisible = true
                }
            }
        }

        viewModel.cats.observe(viewLifecycleOwner) {
            catsAdapter.submitData(viewLifecycleOwner.lifecycle, it)
        }
    }

    override fun onDestroy() {
        Log.d("destroy", "Fragment destroyed")
        super.onDestroy()
        binding = null
    }
}
