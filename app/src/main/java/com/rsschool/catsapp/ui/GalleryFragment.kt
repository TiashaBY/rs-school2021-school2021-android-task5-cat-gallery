package com.rsschool.catsapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rsschool.catsapp.databinding.FragmentGalleryBinding
import com.rsschool.catsapp.model.Cat
import dagger.hilt.android.AndroidEntryPoint

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
        return binding?.root!! //todo
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val catsAdapter = CatsGalleryAdapter(object: CatsGalleryAdapter.OnImageClickListener {
            override fun onItemClick(image: Cat) {
                val action = GalleryFragmentDirections.actionFragmentGalleryToImageDetailsFragment(image)
                findNavController().navigate(action)
            }
        })

        binding?.listRecyclerView?.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
            adapter = catsAdapter.withLoadStateFooter(
                footer = CatsGalleryLoadingStateAdapter{ catsAdapter.retry() }
            )
        }
        viewModel.cats.observe(viewLifecycleOwner) {
            catsAdapter.submitData(viewLifecycleOwner.lifecycle, it)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}